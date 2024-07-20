package com.fit.nufit.food.presentation;

import com.fit.nufit.food.application.FoodNutrientService;
import com.fit.nufit.food.application.FoodService;
import com.fit.nufit.food.dto.request.FoodCreateRequest;
import com.fit.nufit.food.dto.request.FoodNutrientUpdateRequest;
import com.fit.nufit.food.dto.response.FoodResponse;
import com.fit.nufit.food.dto.response.NutrientDetailResponse;
import com.fit.nufit.food.dto.response.SearchFoodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/foods")
@RequiredArgsConstructor
@RestController
public class FoodController {

    private final FoodService foodService;
    private final FoodNutrientService foodNutrientService;

    @GetMapping("/details/{mealDetailId}/nutrients")
    public ResponseEntity<NutrientDetailResponse> findNutrientDetailsByMealDetailId(@PathVariable Long mealDetailId) {
        NutrientDetailResponse response = foodService.getNutrientDetailByMealDetailId(mealDetailId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<List<FoodResponse>> findFoodByMemberId(@PathVariable Long memberId) {
        List<FoodResponse> response = foodService.getFoodsByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/names")
    public ResponseEntity<List<SearchFoodResponse>> findFoodNamesBySearchWord(@RequestParam("q") String searchWord) {
        List<SearchFoodResponse> response = foodService.getFoodNamesBySearchWord(searchWord);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodResponse>> findFoodsBySearchWord(@RequestParam("q") String name,
                                                                    @RequestParam int page) {
        List<FoodResponse> response = foodService.getFoodsByName(name, page);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FoodResponse> createFood(@RequestBody FoodCreateRequest request) {
        FoodResponse response = foodService.save(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{foodId}/nutrients")
    public ResponseEntity<NutrientDetailResponse> updateFoodNutrients(@RequestBody FoodNutrientUpdateRequest request) {
        NutrientDetailResponse response = foodNutrientService.update(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{foodId}")
    public ResponseEntity<Void> deleteFoodAndFoodNutrients(@PathVariable Long foodId) {
        foodService.deleteFoodAndFoodNutrientsById(foodId);
        return ResponseEntity.noContent().build();
    }
}
