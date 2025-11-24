package com.site.elohim.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table( catalog = "smelohim", name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate attendanceDate;
    @Column
    private String name;
    @Column
    private boolean worshipStatus;
    @Column
    private boolean cellStatus;
    @Column
    private String attendanceMemo;
    @Column
    private Long memberId;

}
