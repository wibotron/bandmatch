package com.bandmatch.bandmatch.domain.interaction;

public enum InteractionStatus {
    PENDING,    // Menunggu respon
    ACCEPTED,   // Diterima
    REJECTED,   // Ditolak
    EXPIRED     // Kadaluarsa
}
