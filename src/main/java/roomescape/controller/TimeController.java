package roomescape.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.model.Time;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.List;

@Controller
public class TimeController {

    private final JdbcTemplate jdbcTemplate;

    public TimeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Time> timeRowMapper = (rs, rowNum) -> new Time(
            rs.getLong("id"),
            rs.getString("time")
    );

    // 시간 관리 페이지
    @GetMapping("/time")
    public String reservationPage() {
        return "time";
    }

    // 시간 조회
    @GetMapping("/times")
    @ResponseBody
    public ResponseEntity<List<Time>> getTimes() {
        String sql = "SELECT id, time FROM time";
        List<Time> times = jdbcTemplate.query(sql, timeRowMapper);
        return ResponseEntity.ok(times);
    }

    // 시간 추가
    @PostMapping("/times")
    public ResponseEntity<Time> addTime(@RequestBody Time request) {
        String sql = "INSERT INTO time (time) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, request.getTime());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        Time newTime = new Time(id, request.getTime());

        return ResponseEntity.created(URI.create("/times/" + id)).body(newTime);
    }

    // 시간 삭제
    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable Long id) {
        String sql = "DELETE FROM time WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected > 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
