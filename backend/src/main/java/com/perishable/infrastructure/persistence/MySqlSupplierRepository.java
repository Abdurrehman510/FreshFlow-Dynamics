package com.perishable.infrastructure.persistence;
import com.perishable.domain.model.Supplier;
import com.perishable.domain.model.WastageRecord;
import com.perishable.domain.repository.OrderHistoryRepository;
import com.perishable.domain.repository.SupplierRepository;
import com.perishable.domain.repository.WastageRepository;
import com.perishable.domain.service.DemandForecaster.DailySales;
import com.perishable.domain.valueobject.Money;
import com.perishable.domain.valueobject.ProductCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class MySqlSupplierRepository implements SupplierRepository {

    private static final Logger log = LoggerFactory.getLogger(MySqlSupplierRepository.class);
    private final DataSource dataSource;

    public MySqlSupplierRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Supplier save(Supplier supplier) {
        if (supplier.getId() == 0) return insert(supplier);
        return update(supplier);
    }

    private Supplier insert(Supplier s) {
        String sql = "INSERT INTO suppliers (name, contact_number, email, category, avg_delivery_hours, reliability_score, is_active) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContactNumber());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getCategory().getDbCode());
            ps.setDouble(5, s.getAvgDeliveryTimeHours());
            ps.setDouble(6, s.getReliabilityScore());
            ps.setBoolean(7, s.isActive());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return findById(keys.getInt(1)).orElseThrow();
            throw new RuntimeException("No key returned for supplier insert");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert supplier", e);
        }
    }

    private Supplier update(Supplier s) {
        String sql = "UPDATE suppliers SET avg_delivery_hours=?, reliability_score=?, is_active=? WHERE id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, s.getAvgDeliveryTimeHours());
            ps.setDouble(2, s.getReliabilityScore());
            ps.setBoolean(3, s.isActive());
            ps.setInt(4, s.getId());
            ps.executeUpdate();
            return s;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update supplier", e);
        }
    }

    @Override
    public Optional<Supplier> findById(int id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM suppliers WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        } catch (SQLException e) { return Optional.empty(); }
    }

    @Override
    public List<Supplier> findAll() {
        return queryList("SELECT * FROM suppliers ORDER BY name");
    }

    @Override
    public List<Supplier> findByCategory(ProductCategory category) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM suppliers WHERE category=? ORDER BY reliability_score DESC")) {
            ps.setString(1, category.getDbCode());
            return mapResults(ps.executeQuery());
        } catch (SQLException e) { return List.of(); }
    }

    @Override
    public List<Supplier> findActive() {
        return queryList("SELECT * FROM suppliers WHERE is_active=true ORDER BY reliability_score DESC");
    }

    @Override
    public void deleteById(int id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM suppliers WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Failed to delete supplier", e); }
    }

    private List<Supplier> queryList(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapResults(ps.executeQuery());
        } catch (SQLException e) { return List.of(); }
    }

    private List<Supplier> mapResults(ResultSet rs) throws SQLException {
        List<Supplier> list = new ArrayList<>();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
            rs.getInt("id"), rs.getString("name"), rs.getString("contact_number"),
            rs.getString("email"),
            ProductCategory.fromDbCode(rs.getString("category")).orElse(ProductCategory.FRUITS),
            rs.getDouble("avg_delivery_hours"), rs.getDouble("reliability_score"),
            rs.getBoolean("is_active")
        );
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WastageRepository
// ─────────────────────────────────────────────────────────────────────────────
