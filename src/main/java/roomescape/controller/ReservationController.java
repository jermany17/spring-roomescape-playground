package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.exception.NotFoundReservationException;
import roomescape.model.Reservation;
import roomescape.model.ReservationRequest;
import roomescape.model.Time;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.List;

@Controller
public class ReservationController {

    private final JdbcTemplate jdbcTemplate;

    public ReservationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getString("name"),
            rs.getString("date"),
            new Time(rs.getLong("time_id"), rs.getString("time_value"))
    );

    // 예약 관리 페이지
    @GetMapping("/reservation")
    public String reservationPage() {
        return "new-reservation";
    }

    // 예약 조회
    @GetMapping("/reservations")
    @ResponseBody
    public ResponseEntity<List<Reservation>> getReservations() {
        String sql = """
            SELECT 
                r.id as reservation_id, 
                r.name, 
                r.date, 
                t.id as time_id, 
                t.time as time_value 
            FROM reservation as r 
            INNER JOIN time as t 
            ON r.time_id = t.id
        """;
        List<Reservation> reservations = jdbcTemplate.query(sql, reservationRowMapper);
        return ResponseEntity.ok(reservations);
    }

    // 예약 추가
    @PostMapping("/reservations")
    @ResponseBody
    public ResponseEntity<Reservation> addReservation(@RequestBody ReservationRequest request) {
        request.validateAndSetTimeId(jdbcTemplate);

        // 예약 추가
        String sql = "INSERT INTO reservation (name, date, time_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, request.getName());
            ps.setString(2, request.getDate());
            ps.setLong(3, request.getTimeId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        String timeQuery = "SELECT time FROM time WHERE id = ?";
        String timeValue = jdbcTemplate.queryForObject(timeQuery, String.class, request.getTime());

        Reservation newReservation = new Reservation(id, request.getName(), request.getDate(),
                new Time(request.getTimeId(), timeValue));


        return ResponseEntity.created(URI.create("/reservations/" + id)).body(newReservation);
    }

    // 예약 삭제
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected > 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        throw new NotFoundReservationException("삭제할 예약이 없습니다.");
    }
}
