package com.site.elohim.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table( catalog = "smelohim", name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Members {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String memberName;
    @Column
    private String memberBirth;
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
    @Column
    private boolean cellLeaderStatus;
    @Column
    private Long cellKey;
    @Column
    private String cellName;

}
