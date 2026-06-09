package com.example.walkingservice.controller;

import com.example.walkingservice.dto.FavoriteDto;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final DataSource dataSource;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FavoriteController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 즐겨찾기 추가
    @PostMapping
    public String addFavorite(@RequestBody FavoriteDto dto) {
        String sql = """
                insert into favorites(user_id, walk_id)
                values (?, ?)
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, dto.getUserId());
            stmt.setString(2, dto.getWalkId());
            stmt.executeUpdate();

            return "즐겨찾기 추가 완료";
        } catch (SQLException e) {
            e.printStackTrace();
            return "즐겨찾기 추가 실패: 이미 추가했거나 존재하지 않는 산책로입니다.";
        }
    }

    // 사용자별 즐겨찾기 목록 조회
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getFavoritesByUser(@PathVariable String userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
                select
                    f.favorite_id,
                    f.user_id,
                    w1.id as walk_id,
                    w1.trail_name,
                    w1.course_name,
                    w2.course_km,
                    w2.difficulty,
                    w3.address,
                    f.created_at
                from favorites f
                join walk1 w1 on f.walk_id = w1.id
                join walk2 w2 on f.walk_id = w2.id
                join walk3 w3 on f.walk_id = w3.id
                where f.user_id = ?
                order by f.created_at desc
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("favoriteId", rs.getInt("favorite_id"));
                    row.put("userId", rs.getString("user_id"));
                    row.put("walkId", rs.getString("walk_id"));
                    row.put("trailName", rs.getString("trail_name"));
                    row.put("courseName", rs.getString("course_name"));
                    row.put("courseKm", rs.getDouble("course_km"));
                    row.put("difficulty", rs.getString("difficulty"));
                    row.put("address", rs.getString("address"));

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        row.put("createdAt", createdAt.toLocalDateTime().plusHours(9).format(formatter));
                    } else {
                        row.put("createdAt", null);
                    }

                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // 즐겨찾기 삭제
    @DeleteMapping("/{favoriteId}")
    public String deleteFavorite(@PathVariable int favoriteId) {
        String sql = "delete from favorites where favorite_id = ?";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, favoriteId);
            int count = stmt.executeUpdate();

            if (count > 0) {
                return "즐겨찾기 삭제 완료";
            } else {
                return "삭제할 즐겨찾기를 찾을 수 없습니다.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "즐겨찾기 삭제 실패";
        }
    }

    // 즐겨찾기 많은 산책로 조회
    @GetMapping("/popular")
    public List<Map<String, Object>> getPopularFavorites() {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.trail_name,
                    w1.course_name,
                    count(f.favorite_id) as favorite_count
                from favorites f
                join walk1 w1 on f.walk_id = w1.id
                group by w1.id, w1.trail_name, w1.course_name
                order by favorite_count desc
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("walkId", rs.getString("id"));
                row.put("trailName", rs.getString("trail_name"));
                row.put("courseName", rs.getString("course_name"));
                row.put("favoriteCount", rs.getInt("favorite_count"));

                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}