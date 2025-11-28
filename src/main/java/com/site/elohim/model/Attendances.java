package com.site.elohim.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        catalog = "smelohim",
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_memberId_attendanceDate",
                        columnNames = {"memberId", "attendanceDate"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 숫자 FK 그대로 유지
    @Column(name = "memberId", nullable = false)
    private Long memberId;
    // 실제 연관관계 (Members)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", insertable = false, updatable = false)
    private Members member;
    @Column(nullable = false)
    private LocalDate attendanceDate;
    @Column(nullable = false)
    private boolean worshipStatus;
    @Column(nullable = false)
    private boolean cellStatus;
    @Column
    private String attendanceMemo;
}
