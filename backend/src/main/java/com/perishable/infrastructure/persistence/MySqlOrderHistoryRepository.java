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
public class MySqlOrderHistoryRepository implements OrderHistoryRepository {

    private final DataSource dataSource;

    public MySqlOrderHistoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<DailySales> getDailySalesHistory(int productId, int lastNDays) {
        String sql = """
            SELECT DATE(o.placed_at) AS sale_date, SUM(ol.quantity) AS units_sold
            FROM order_lines ol
            JOIN orders o ON ol.order_id = o.id
            WHERE ol.product_id = ?
              AND o.placed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
              AND o.status != 'CANCELLED'
            GROUP BY DATE(o.placed_at)
            ORDER BY sale_date DESC
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, lastNDays);
            ResultSet rs = ps.executeQuery();
            List<DailySales> history = new ArrayList<>();
            while (rs.next()) {
                history.add(new DailySales(
                    rs.getDate("sale_date").toLocalDate(),
                    rs.getInt("units_sold")
                ));
            }
            return history;
        } catch (SQLException e) {
            return List.of();
        }
    }
}
