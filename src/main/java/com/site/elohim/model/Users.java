package com.site.elohim.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        catalog = "smelohim",
        name = "loginuser",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_userId", columnNames = "userId")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userRole;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String userPassword;
    @Column(nullable = false)
    private String userName;

    // 숫자 FK 그대로 유지
    @Column(name = "memberId")
    private Long memberId;

    // 실제 연관관계 (Members)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", insertable = false, updatable = false)
    private Members member;
}
