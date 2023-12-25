package spring.rest.shop.springrestshop.dto.shop;

import lombok.Data;
import spring.rest.shop.springrestshop.entity.Organization;

@Data
public class ShopEditDTO {
    private String name;
    private String description;

    public ShopEditDTO(Organization shop) {
        this.name = shop.getName();
        this.description = shop.getDescription();
    }
    public ShopEditDTO() {
    }
}

