package com.example.walkingservice.controller;

import com.example.walkingservice.dto.ReviewDto;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final DataSource dataSource;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReviewController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 후기 작성
    @PostMapping
    public String addReview(@RequestBody ReviewDto dto) {
        String sql = """
                insert into reviews(walk_id, user_id, rating, content)
                values (?, ?, ?, ?)
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, dto.getWalkId());
            stmt.setString(2, dto.getUserId());
            stmt.setInt(3, dto.getRating());
            stmt.setString(4, dto.getContent());
            stmt.executeUpdate();

            return "후기 등록 완료";
        } catch (SQLException e) {
            e.printStackTrace();
            return "후기 등록 실패: 이미 작성했거나 평점/산책로 정보가 올바르지 않습니다.";
        }
    }

    // 산책로별 후기 조회
    @GetMapping("/walk/{walkId}")
    public List<Map<String, Object>> getReviewsByWalk(@PathVariable String walkId) {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
                select
                    review_id,
                    walk_id,
                    user_id,
                    rating,
                    content,
                    created_at
                from reviews
                where walk_id = ?
                order by created_at desc
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, walkId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("reviewId", rs.getInt("review_id"));
                    row.put("walkId", rs.getString("walk_id"));
                    row.put("userId", rs.getString("user_id"));
                    row.put("rating", rs.getInt("rating"));
                    row.put("content", rs.getString("content"));

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

    // 평점 높은 산책로 조회
    @GetMapping("/top-rated")
    public List<Map<String, Object>> getTopRatedWalks() {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.trail_name,
                    w1.course_name,
                    round(avg(r.rating), 1) as avg_rating,
                    count(r.review_id) as review_count
                from reviews r
                join walk1 w1 on r.walk_id = w1.id
                group by w1.id, w1.trail_name, w1.course_name
                order by avg_rating desc, review_count desc
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
                row.put("avgRating", rs.getDouble("avg_rating"));
                row.put("reviewCount", rs.getInt("review_count"));

                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // 후기 삭제
    @DeleteMapping("/{reviewId}")
    public String deleteReview(@PathVariable int reviewId) {
        String sql = "delete from reviews where review_id = ?";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, reviewId);
            int count = stmt.executeUpdate();

            if (count > 0) {
                return "후기 삭제 완료";
            } else {
                return "삭제할 후기를 찾을 수 없습니다.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "후기 삭제 실패";
        }
    }
}