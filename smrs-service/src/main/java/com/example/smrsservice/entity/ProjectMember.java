package com.example.smrsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String status = "Pending";

    @Column(name = "member_role")
    private String memberRole; // "TEACHER", "STUDENT"


    // pending  , approve thì tức là nó đồng vo nhóm , camcel  // compeple

//    khi mà thằng user nó change status của cái bảng ny pending => chuyển status từ pending sang approve hoặc là cancel
//    // api user approve  id cả thagnừ git all trong bảng này  id / project id  get by theo account vs proj  =>  em chuyển status cảu bẳng sang approve nếu mà cái này đang approve thì không chuyển lại cencel \
//    => get all bảng này theo account Id  tât cả cái recỏd liên account id này  vs trạng thái pending  chueyẻn thành cacnel hết
//    => project hoàng hành

//    bảng ađ thằng user em muối mời zo trồng project  ( email, id project )  email account  tại thời đó em cmail service em gửi maill tới cái email của thằng đó

}