package com.example.qnr.resources;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders", indexes = {@Index(name = "idx_orders_status", columnList = "status")})
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", unique = true, nullable = false, updatable = false)
    private Integer orderId;
    @Column(name = "description")
    private String description;
    @Column(name = "status")
    private String status;
    @CreationTimestamp
    @Column(name = "create_date")
    private LocalDateTime createDate;

}
