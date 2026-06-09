package com.example.walkingservice.controller;

import com.example.walkingservice.dto.WalkingDto;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/walks")
public class WalkingController {

    private final DataSource dataSource;

    public WalkingController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public List<WalkingDto> getAllWalks() {
        List<WalkingDto> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.course_id,
                    w1.trail_name,
                    w1.course_name,
                    w1.course_desc,
                    w1.district_name,
                    w2.difficulty,
                    w2.course_km,
                    w2.hour,
                    w2.minute,
                    w2.minutes,
                    w2.water,
                    w2.toilet,
                    w3.address,
                    w3.lat,
                    w3.lng
                from walk1 w1
                join walk2 w2 on w1.id = w2.id
                join walk3 w3 on w1.id = w3.id
                limit 50
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                result.add(makeWalkingDto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/{walkId}")
    public WalkingDto getWalkById(@PathVariable String walkId) {
        String sql = """
                select
                    w1.id,
                    w1.course_id,
                    w1.trail_name,
                    w1.course_name,
                    w1.course_desc,
                    w1.district_name,
                    w2.difficulty,
                    w2.course_km,
                    w2.hour,
                    w2.minute,
                    w2.minutes,
                    w2.water,
                    w2.toilet,
                    w3.address,
                    w3.lat,
                    w3.lng
                from walk1 w1
                join walk2 w2 on w1.id = w2.id
                join walk3 w3 on w1.id = w3.id
                where w1.id = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, walkId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return makeWalkingDto(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @GetMapping("/short")
    public List<WalkingDto> getShortWalks(@RequestParam Double maxKm) {
        List<WalkingDto> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.course_id,
                    w1.trail_name,
                    w1.course_name,
                    w1.course_desc,
                    w1.district_name,
                    w2.difficulty,
                    w2.course_km,
                    w2.hour,
                    w2.minute,
                    w2.minutes,
                    w2.water,
                    w2.toilet,
                    w3.address,
                    w3.lat,
                    w3.lng
                from walk1 w1
                join walk2 w2 on w1.id = w2.id
                join walk3 w3 on w1.id = w3.id
                where w2.course_km <= ?
                order by w2.course_km asc
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setDouble(1, maxKm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(makeWalkingDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/difficulty")
    public List<WalkingDto> getWalksByDifficulty(@RequestParam String level) {
        List<WalkingDto> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.course_id,
                    w1.trail_name,
                    w1.course_name,
                    w1.course_desc,
                    w1.district_name,
                    w2.difficulty,
                    w2.course_km,
                    w2.hour,
                    w2.minute,
                    w2.minutes,
                    w2.water,
                    w2.toilet,
                    w3.address,
                    w3.lat,
                    w3.lng
                from walk1 w1
                join walk2 w2 on w1.id = w2.id
                join walk3 w3 on w1.id = w3.id
                where w2.difficulty = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, level);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(makeWalkingDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/facility")
    public List<WalkingDto> getWalksByToilet(@RequestParam String toilet) {
        List<WalkingDto> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.course_id,
                    w1.trail_name,
                    w1.course_name,
                    w1.course_desc,
                    w1.district_name,
                    w2.difficulty,
                    w2.course_km,
                    w2.hour,
                    w2.minute,
                    w2.minutes,
                    w2.water,
                    w2.toilet,
                    w3.address,
                    w3.lat,
                    w3.lng
                from walk1 w1
                join walk2 w2 on w1.id = w2.id
                join walk3 w3 on w1.id = w3.id
                where w2.toilet = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, toilet);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(makeWalkingDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/water")
    public List<WalkingDto> getWalksByWater(@RequestParam String water) {
        List<WalkingDto> result = new ArrayList<>();

        String sql = """
                select
                    w1.id,
                    w1.course_id,
                    w1.trail_name,
                    w1.course_name,
                    w1.course_desc,
                    w1.district_name,
                    w2.difficulty,
                    w2.course_km,
                    w2.hour,
                    w2.minute,
                    w2.minutes,
                    w2.water,
                    w2.toilet,
                    w3.address,
                    w3.lat,
                    w3.lng
                from walk1 w1
                join walk2 w2 on w1.id = w2.id
                join walk3 w3 on w1.id = w3.id
                where w2.water = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, water);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(makeWalkingDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/tags")
    public List<WalkingDto> getWalksByTag(@RequestParam String tag) {
        List<WalkingDto> result = new ArrayList<>();

        String sql = """
            select
                w1.id,
                w1.course_id,
                w1.trail_name,
                w1.course_name,
                w1.course_desc,
                w1.district_name,
                w2.difficulty,
                w2.course_km,
                w2.hour,
                w2.minute,
                w2.minutes,
                w2.water,
                w2.toilet,
                w3.address,
                w3.lat,
                w3.lng
            from walk_tags t
            join walk1 w1 on t.walk_id = w1.id
            join walk2 w2 on t.walk_id = w2.id
            join walk3 w3 on t.walk_id = w3.id
            where t.tag_name = ?
            """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, tag);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(makeWalkingDto(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private WalkingDto makeWalkingDto(ResultSet rs) throws SQLException {
        return new WalkingDto(
                rs.getString("id"),
                rs.getString("course_id"),
                rs.getString("trail_name"),
                rs.getString("course_name"),
                rs.getString("course_desc"),
                rs.getString("district_name"),
                rs.getString("difficulty"),
                rs.getDouble("course_km"),
                rs.getInt("hour"),
                rs.getInt("minute"),
                rs.getInt("minutes"),
                rs.getString("water"),
                rs.getString("toilet"),
                rs.getString("address"),
                rs.getDouble("lat"),
                rs.getDouble("lng")
        );
    }
}