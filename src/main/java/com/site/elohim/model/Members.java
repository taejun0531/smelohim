package com.site.elohim.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        catalog = "smelohim",
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_member_name_birth",
                        columnNames = {"memberName", "memberBirth"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Members {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String memberName;
    @Column
    private LocalDate memberBirth;
    @Column
    private String memberPhoneNumber;
    @Column
    private String memberAddress;
    @Column
    private String baptismStatus;
    @Column
    private String worshipStatus;
    @Column
    private String cellStatus;
    @Column
    private String nurtureYear;
    @Column
    private String nurtureSemester;
    @Column
    private String growthYear;
    @Column
    private String growthSemester;
    @Column
    private String memberMemo;
    @Column(nullable = false)
    private boolean cellLeaderStatus;

    // ===== self FK: cellKey =====
    // 숫자 FK 그대로 쓰고 싶을 때를 위해 유지
    @Column(name = "cellKey")
    private Long cellKey;

    // 셀 리더 (자기 자신을 참조하는 ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cellKey", insertable = false, updatable = false)
    @JsonIgnore        // 🔹 순환 방지
    private Members cellLeader;

    @Column
    private String cellName;

    // 이 멤버를 셀 리더로 참조하는 멤버들
    @OneToMany(mappedBy = "cellLeader")
    @JsonIgnore        // 🔹 순환 방지
    private List<Members> cellMembers = new ArrayList<>();

    // 이 멤버의 출석 목록
    @OneToMany(mappedBy = "member")
    @JsonIgnore        // 🔹 순환 방지
    private List<Attendances> attendances = new ArrayList<>();

}
