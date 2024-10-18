package com.datn.tourhotel.model;

import com.datn.tourhotel.model.enums.RoomType;

import jakarta.mail.search.DateTerm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction_notification")
public class TransactionNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "gateway", nullable = false, length = 100)
    private String gateway;

    @Column(name = "transaction_date", nullable = false)
    private Date transactionDate;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "sub_account", length = 250)
    private String subAccount;

    @Column(name = "amount_in", nullable = false)
    private Double amountIn;

    @Column(name = "amount_out", nullable = false)
    private Double amountOut;

    @Column(name = "accumulated", nullable = false)
    private BigDecimal accumulated;

    @Column(name = "code", length = 250)
    private String code;

    @Column(name = "transaction_content", columnDefinition = "TEXT")
    private String transactionContent;

    @Column(name = "reference_number", length = 255)
    private String referenceNumber;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Date createdAt;
}