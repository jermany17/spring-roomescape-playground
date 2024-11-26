package roomescape.model;

public class Reservation {
    private Long id;
    private String name;
    private String date;
    private String time;

    // 기본 생성자를 추가 하는 이유
        // 1. Jackson과의 호환성 및 역직렬화 원활
        // 2. 객체를 생성할 때 기본 생서자로 빈 객체를 만든 후 필드를 하나씩 채워 넣는다.
    // 기본 생성자를 추가하지 않고 @JsonCreator와 @JsonProperty를 사용하는 방법도 있음.
    public Reservation() {
    }

    public Reservation(Long id, String name, String date, String time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}

