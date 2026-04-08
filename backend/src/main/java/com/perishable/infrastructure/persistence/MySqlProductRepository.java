package com.perishable.infrastructure.persistence;

import com.perishable.domain.model.Product;
import com.perishable.domain.repository.ProductRepository;
import com.perishable.domain.valueobject.ExpiryDate;
import com.perishable.domain.valueobject.Money;
import com.perishable.domain.valueobject.ProductCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlProductRepository implements ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(MySqlProductRepository.class);
    private final DataSource dataSource;

    public MySqlProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Product save(Product product) {
        return product.getId() == 0 ? insert(product) : update(product);
    }

    private Product insert(Product product) {
        String sql = """
            INSERT INTO products (name, description, base_price, dynamic_price, category,
                expiry_date, stock_quantity, units_sold_total, units_wasted_total, supplier_id, added_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getBasePrice().amount());
            ps.setBigDecimal(4, product.getCurrentDynamicPrice().amount());
            ps.setString(5, product.getCategory().getDbCode());
            ps.setDate(6, Date.valueOf(product.getExpiryDate().value()));
            ps.setInt(7, product.getStockQuantity());
            ps.setInt(8, product.getUnitsSoldTotal());
            ps.setInt(9, product.getUnitsWastedTotal());
            ps.setInt(10, product.getSupplierId());
            ps.setTimestamp(11, Timestamp.valueOf(product.getAddedAt()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return findById(keys.getInt(1)).orElseThrow();
            throw new RuntimeException("Product insert failed");

        } catch (SQLException e) {
            log.error("Failed to insert product: {}", e.getMessage());
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
    }

    private Product update(Product product) {
        String sql = """
            UPDATE products SET name=?, description=?, base_price=?, dynamic_price=?,
                expiry_date=?, stock_quantity=?, units_sold_total=?, units_wasted_total=?,
                last_updated_at=NOW()
            WHERE id=?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getBasePrice().amount());
            ps.setBigDecimal(4, product.getCurrentDynamicPrice().amount());
            ps.setDate(5, Date.valueOf(product.getExpiryDate().value()));
            ps.setInt(6, product.getStockQuantity());
            ps.setInt(7, product.getUnitsSoldTotal());
            ps.setInt(8, product.getUnitsWastedTotal());
            ps.setInt(9, product.getId());
            ps.executeUpdate();
            return product;

        } catch (SQLException e) {
            log.error("Failed to update product {}: {}", product.getId(), e.getMessage());
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            log.error("Failed to find product {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        return queryList("SELECT * FROM products ORDER BY name");
    }

    @Override
    public List<Product> findByCategory(ProductCategory category) {
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY dynamic_price";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getDbCode());
            return mapResults(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Failed to find by category: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Product> findAvailable() {
        String sql = "SELECT * FROM products WHERE stock_quantity > 0 AND expiry_date >= CURDATE() ORDER BY expiry_date";
        return queryList(sql);
    }

    @Override
    public List<Product> findExpiringSoon(int withinDays) {
        String sql = """
            SELECT * FROM products
            WHERE stock_quantity > 0
              AND expiry_date >= CURDATE()
              AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
            ORDER BY expiry_date
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, withinDays);
            return mapResults(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Failed to find expiring products: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Product> findExpired() {
        String sql = "SELECT * FROM products WHERE expiry_date < CURDATE() AND stock_quantity > 0";
        return queryList(sql);
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    public void updateDynamicPrice(int productId, double newPrice) {
        String sql = "UPDATE products SET dynamic_price = ?, last_updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newPrice);
            ps.setInt(2, productId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update dynamic price", e);
        }
    }

    @Override
    public void updateStock(int productId, int newQuantity) {
        String sql = "UPDATE products SET stock_quantity = ?, last_updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stock", e);
        }
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Product> searchByNameOrDescription(String query) {
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ? ORDER BY name";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + query.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            return mapResults(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Product> queryList(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return mapResults(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Query failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Product> mapResults(ResultSet rs) throws SQLException {
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            Money.of(rs.getBigDecimal("base_price").doubleValue()),
            Money.of(rs.getBigDecimal("dynamic_price").doubleValue()),
            ProductCategory.fromDbCode(rs.getString("category"))
                .orElse(ProductCategory.FRUITS),
            ExpiryDate.of(rs.getDate("expiry_date").toLocalDate()),
            rs.getInt("stock_quantity"),
            rs.getInt("units_sold_total"),
            rs.getInt("units_wasted_total"),
            rs.getInt("supplier_id"),
            rs.getTimestamp("added_at") != null ? rs.getTimestamp("added_at").toLocalDateTime() : null,
            rs.getTimestamp("last_updated_at") != null ? rs.getTimestamp("last_updated_at").toLocalDateTime() : null
        );
    }
}
