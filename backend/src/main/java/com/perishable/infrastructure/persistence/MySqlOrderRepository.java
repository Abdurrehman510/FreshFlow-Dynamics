package com.perishable.infrastructure.persistence;

import com.perishable.domain.model.Order;
import com.perishable.domain.model.Order.OrderLine;
import com.perishable.domain.repository.OrderRepository;
import com.perishable.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlOrderRepository implements OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(MySqlOrderRepository.class);
    private final DataSource dataSource;

    public MySqlOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Order save(Order order) {
        // Uses explicit transaction — order + order_lines saved atomically
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int orderId = insertOrder(conn, order);
                insertOrderLines(conn, orderId, order.getLines());
                conn.commit();
                log.info("Order {} saved successfully", orderId);
                return findById(orderId).orElseThrow();

            } catch (SQLException e) {
                conn.rollback();
                log.error("Order save failed — rolled back: {}", e.getMessage());
                throw new RuntimeException("Failed to save order", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection for order save", e);
        }
    }

    private int insertOrder(Connection conn, Order order) throws SQLException {
        String sql = """
            INSERT INTO orders (user_id, total_amount, payment_method, status, placed_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotalAmount().amount());
            ps.setString(3, order.getPaymentMethod().name());
            ps.setString(4, order.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(order.getPlacedAt()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("No generated key for order insert");
        }
    }

    private void insertOrderLines(Connection conn, int orderId, List<OrderLine> lines) throws SQLException {
        String sql = """
            INSERT INTO order_lines (order_id, product_id, product_name, quantity, price_per_unit)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderLine line : lines) {
                ps.setInt(1, orderId);
                ps.setInt(2, line.productId());
                ps.setString(3, line.productName());
                ps.setInt(4, line.quantity());
                ps.setBigDecimal(5, line.pricePerUnit().amount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<Order> findById(int id) {
        String sql = """
            SELECT o.*, ol.product_id, ol.product_name, ol.quantity, ol.price_per_unit
            FROM orders o
            JOIN order_lines ol ON o.id = ol.order_id
            WHERE o.id = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return buildOrderFromResultSet(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Failed to find order {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findByUserId(int userId) {
        String sql = """
            SELECT o.*, ol.product_id, ol.product_name, ol.quantity, ol.price_per_unit
            FROM orders o
            JOIN order_lines ol ON o.id = ol.order_id
            WHERE o.user_id = ?
            ORDER BY o.placed_at DESC
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return buildOrderListFromResultSet(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Failed to find orders for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = """
            SELECT o.*, ol.product_id, ol.product_name, ol.quantity, ol.price_per_unit
            FROM orders o
            JOIN order_lines ol ON o.id = ol.order_id
            ORDER BY o.placed_at DESC
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return buildOrderListFromResultSet(ps.executeQuery());

        } catch (SQLException e) {
            log.error("Failed to list all orders: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateStatus(int orderId, Order.OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }

    private Optional<Order> buildOrderFromResultSet(ResultSet rs) throws SQLException {
        List<Order> orders = buildOrderListFromResultSet(rs);
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    private List<Order> buildOrderListFromResultSet(ResultSet rs) throws SQLException {
        List<Order> orders = new ArrayList<>();
        int currentOrderId = -1;
        List<OrderLine> currentLines = new ArrayList<>();
        Order.PaymentMethod paymentMethod = null;
        Order.OrderStatus status = null;
        int userId = -1;
        Money total = Money.ZERO;
        Timestamp placedAt = null;

        while (rs.next()) {
            int orderId = rs.getInt("id");
            if (orderId != currentOrderId) {
                if (currentOrderId != -1) {
                    orders.add(new Order(currentOrderId, userId, new ArrayList<>(currentLines),
                        total, paymentMethod, status, placedAt.toLocalDateTime()));
                    currentLines.clear();
                }
                currentOrderId = orderId;
                userId = rs.getInt("user_id");
                total = Money.of(rs.getBigDecimal("total_amount").doubleValue());
                paymentMethod = Order.PaymentMethod.valueOf(rs.getString("payment_method"));
                status = Order.OrderStatus.valueOf(rs.getString("status"));
                placedAt = rs.getTimestamp("placed_at");
            }
            currentLines.add(new OrderLine(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getInt("quantity"),
                Money.of(rs.getBigDecimal("price_per_unit").doubleValue())
            ));
        }
        if (currentOrderId != -1) {
            orders.add(new Order(currentOrderId, userId, currentLines,
                total, paymentMethod, status, placedAt.toLocalDateTime()));
        }
        return orders;
    }
}
