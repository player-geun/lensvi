package com.fit.nufit.food.dto.response;

import com.fit.nufit.food.domain.Food;
import com.fit.nufit.food.domain.FoodType;
import com.fit.nufit.food.domain.FoodUnit;
import com.fit.nufit.nutrient.dto.response.NutrientResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FoodResponse {

    private Long id;
    private String name;
    private String brand;
    private int amount;
    private FoodType type;
    private FoodUnit unit;
    private int calorie;
    private List<NutrientResponse> nutrientResponses;

    public FoodResponse(Food food, List<NutrientResponse> nutrientResponses) {
        this.id = food.getId();
        this.name = food.getName();
        this.brand = food.getBrand();
        this.amount = food.getAmount();
        this.type = food.getType();
        this.unit = food.getUnit();
        this.calorie = (int)Math.round(food.getCalorie());
        this.nutrientResponses = nutrientResponses;
    }
}
