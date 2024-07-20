package com.fit.nufit.food.application;

import com.fit.nufit.food.domain.Food;
import com.fit.nufit.food.domain.FoodNutrient;
import com.fit.nufit.food.domain.FoodNutrientRepository;
import com.fit.nufit.food.domain.FoodRepository;
import com.fit.nufit.food.dto.request.FoodCreateRequest;
import com.fit.nufit.food.dto.request.FoodNutrientCreateRequest;
import com.fit.nufit.food.dto.response.FoodResponse;
import com.fit.nufit.food.dto.response.NutrientDetailResponse;
import com.fit.nufit.food.dto.response.SearchFoodResponse;
import com.fit.nufit.meal.domain.MealDetail;
import com.fit.nufit.meal.domain.MealDetailRepository;
import com.fit.nufit.member.domain.MemberRepository;
import com.fit.nufit.nutrient.domain.Nutrient;
import com.fit.nufit.nutrient.domain.NutrientRepository;
import com.fit.nufit.nutrient.dto.response.NutrientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FoodService {

    private final MemberRepository memberRepository;

    private final FoodRepository foodRepository;

    private final MealDetailRepository mealDetailRepository;

    private final FoodNutrientRepository foodNutrientRepository;

    private final NutrientRepository nutrientRepository;

    @Transactional
    public FoodResponse save(FoodCreateRequest request) {
        Food food = foodRepository.save(Food.toEntity(request));
        food.changeWriter(memberRepository.getById(request.getMemberId()));
        List<FoodNutrientCreateRequest> nutrients = request.getNutrients();
        createFoodNutrients(food, nutrients);
        return new FoodResponse(food);
    }

    public void createFoodNutrients(Food food, List<FoodNutrientCreateRequest> nutrients) {
        for (FoodNutrientCreateRequest nutrient : nutrients) {
            String name = nutrient.getName();
            Nutrient findNutrient = nutrientRepository.getByName(name);
            FoodNutrient foodNutrient = new FoodNutrient(food, findNutrient, nutrient.getAmount());
            foodNutrientRepository.save(foodNutrient);
        }
    }

    public FoodResponse findById(Long id) {
        Food findFood = foodRepository.getById(id);
        return new FoodResponse(findFood);
    }

    public NutrientDetailResponse getNutrientDetailByMealDetailId(Long mealDetailId) {
        MealDetail mealDetail = mealDetailRepository.getById(mealDetailId);
        int foodCount = mealDetail.getFoodCount();
        Food food = mealDetail.getFood();
        String foodName = food.getName();
        int calorieTotal = (int) Math.round(food.getCalorie() * foodCount);
        List<NutrientResponse> nutrientResponses = getNutrientResponses(food.getId(), foodCount);

        return new NutrientDetailResponse(foodName, calorieTotal, nutrientResponses);
    }

    public List<NutrientResponse> getNutrientResponses(Long foodId, int foodCount) {
        List<FoodNutrient> foodNutrients = foodNutrientRepository.findByFoodId(foodId);
        Map<Long, NutrientResponse> parentNutrientResponses = new HashMap<>();

        //상위 영양소인 경우
        setParentNutrientResponses(foodCount, foodNutrients, parentNutrientResponses);

        //하위 영양소인 경우
        setChildNutrientResponses(foodCount, foodNutrients, parentNutrientResponses);

        List<NutrientResponse> nutrientResponses = new ArrayList<>(parentNutrientResponses.values());
        return nutrientResponses;
    }

    private static void setParentNutrientResponses(int foodCount, List<FoodNutrient> foodNutrients, Map<Long, NutrientResponse> parentNutrientResponses) {
        for (FoodNutrient foodNutrient : foodNutrients) {
            Nutrient nutrient = foodNutrient.getNutrient();
            Nutrient parentNutrient = nutrient.getParentNutrient();
            if (parentNutrient == null) {
                double totalNutrientAmount = foodNutrient.getAmount() * foodCount;
                parentNutrientResponses.put(nutrient.getId(), new NutrientResponse(nutrient, (int) Math.round(totalNutrientAmount)));
            }
        }
    }

    private static void setChildNutrientResponses(int foodCount, List<FoodNutrient> foodNutrients, Map<Long, NutrientResponse> parentNutrientResponses) {
        for (FoodNutrient foodNutrient : foodNutrients) {
            Nutrient nutrient = foodNutrient.getNutrient();
            Nutrient parentNutrient = nutrient.getParentNutrient();
            if (parentNutrient != null) {
                double totalNutrientAmount = foodNutrient.getAmount() * foodCount;
                NutrientResponse nutrientResponse = parentNutrientResponses.get(parentNutrient.getId());
                nutrientResponse.addChildNutrientResponses(new NutrientResponse(nutrient, (int) Math.round(totalNutrientAmount)));
            }
        }
    }

    public List<FoodResponse> getFoodsByMemberId(Long memberId) {
        List<Food> foods = foodRepository.getByMemberId(memberId);
        return foods.stream()
                .map(FoodResponse::new)
                .collect(Collectors.toList());
    }

    public List<FoodResponse> getFoodsByName(String name, int page) {
        PageRequest pageRequest = PageRequest.of(page, 10);
        List<Food> foods = foodRepository.getFoodsByName(name, pageRequest);
        return foods.stream()
                .map(FoodResponse::new)
                .collect(Collectors.toList());
    }

    public List<SearchFoodResponse> getFoodNamesBySearchWord(String searchWord) {
        PageRequest pageRequest = PageRequest.of(0, 10);
        return foodRepository.getFoodNamesBySearchWord(searchWord, pageRequest);
    }

    @Transactional
    public void deleteFoodAndFoodNutrientsById(Long id) {
        foodNutrientRepository.deleteAllByFoodId(id);
        foodRepository.deleteById(id);
    }
}
