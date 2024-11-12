package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.exception.NotFoundReservationException;
import roomescape.model.Reservation;
import roomescape.model.ReservationRequest;

import java.net.URI;
import java.util.List;

@Controller
public class ReservationController {

    private final JdbcTemplate jdbcTemplate;

    public ReservationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("date"),
            rs.getString("time")
    );

    // 예약 관리 페이지
    @GetMapping("/reservation")
    public String reservationPage() {
        return "reservation";
    }

    // 예약 조회
    @GetMapping("/reservations")
    @ResponseBody
    public List<Reservation> getReservations() {
        String sql = "SELECT id, name, date, time FROM reservation";
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    // 예약 추가
    @PostMapping("/reservations")
    public ResponseEntity<Reservation> addReservation(@RequestBody ReservationRequest request) {
        request.validate();
        Reservation newReservation = new Reservation(null, request.getName(), request.getDate(), request.getTime());

        String sql = "INSERT INTO reservation (name, date, time) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, newReservation.getName(), newReservation.getDate(), newReservation.getTime());

        Long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        newReservation.setId(id);

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
