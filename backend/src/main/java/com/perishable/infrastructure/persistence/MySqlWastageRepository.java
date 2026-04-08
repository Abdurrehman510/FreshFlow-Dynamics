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
public class MySqlWastageRepository implements WastageRepository {

    private final DataSource dataSource;

    public MySqlWastageRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(WastageRecord record) {
        String sql = "INSERT INTO wastage_records (product_id, product_name, units_wasted, value_wasted, recorded_on, reason) VALUES (?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.productId());
            ps.setString(2, record.productName());
            ps.setInt(3, record.unitsWasted());
            ps.setBigDecimal(4, record.valueWasted().amount());
            ps.setDate(5, Date.valueOf(record.recordedOn()));
            ps.setString(6, record.reason());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save wastage record", e);
        }
    }

    @Override
    public List<WastageRecord> findBetween(LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM wastage_records WHERE recorded_on BETWEEN ? AND ? ORDER BY recorded_on DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            return mapResults(ps.executeQuery());
        } catch (SQLException e) { return List.of(); }
    }

    @Override
    public List<WastageRecord> findByProductId(int productId) {
        String sql = "SELECT * FROM wastage_records WHERE product_id = ? ORDER BY recorded_on DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return mapResults(ps.executeQuery());
        } catch (SQLException e) { return List.of(); }
    }

    private List<WastageRecord> mapResults(ResultSet rs) throws SQLException {
        List<WastageRecord> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new WastageRecord(
                rs.getInt("id"), rs.getInt("product_id"), rs.getString("product_name"),
                rs.getInt("units_wasted"),
                Money.of(rs.getBigDecimal("value_wasted").doubleValue()),
                rs.getDate("recorded_on").toLocalDate(), rs.getString("reason")
            ));
        }
        return list;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OrderHistoryRepository — for demand forecasting
// ─────────────────────────────────────────────────────────────────────────────
