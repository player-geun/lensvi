package com.fit.nufit.food.dto.response;

import com.fit.nufit.food.domain.Food;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SearchFoodResponse {

    private Long id;
    private String name;

    public SearchFoodResponse(Food food) {
        this.id = food.getId();
        this.name = food.getName();
    }
}
