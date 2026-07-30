package com.shopstack.modules.wishlist.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shopstack.common.audit.BaseEntity;
import com.shopstack.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist extends BaseEntity {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishlistItem> items = new ArrayList<>();

    public void addItem(WishlistItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        if (!items.contains(item)) {
            items.add(item);
            item.setWishlist(this);
        }
    }

    public void removeItem(WishlistItem item) {
        if (items != null) {
            items.remove(item);
            item.setWishlist(null);
        }
    }

    public List<WishlistItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
}