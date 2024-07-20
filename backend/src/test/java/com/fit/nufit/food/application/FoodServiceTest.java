package com.fit.nufit.food.application;

import com.fit.nufit.food.domain.*;
import com.fit.nufit.food.dto.request.FoodCreateRequest;
import com.fit.nufit.food.dto.request.FoodNutrientCreateRequest;
import com.fit.nufit.food.dto.response.FoodResponse;
import com.fit.nufit.food.dto.response.NutrientDetailResponse;
import com.fit.nufit.food.dto.response.SearchFoodResponse;
import com.fit.nufit.food.exception.NoSuchFoodException;
import com.fit.nufit.meal.domain.*;
import com.fit.nufit.member.domain.Member;
import com.fit.nufit.member.domain.MemberRepository;
import com.fit.nufit.member.domain.Role;
import com.fit.nufit.nutrient.domain.Nutrient;
import com.fit.nufit.nutrient.domain.NutrientRepository;
import com.fit.nufit.nutrient.domain.NutrientUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FoodServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FoodService foodService;

    @Autowired
    NutrientRepository nutrientRepository;

    @Autowired
    FoodRepository foodRepository;

    @Autowired
    FoodNutrientRepository foodNutrientRepository;

    @Autowired
    MealDetailRepository mealDetailRepository;

    @Autowired
    MealRepository mealRepository;

    @BeforeEach
    void beforeEach() {
        Nutrient carb = nutrientRepository.save(new Nutrient("탄수화물", NutrientUnit.G));
        nutrientRepository.save(new Nutrient("지방", NutrientUnit.G));
        nutrientRepository.save(new Nutrient("단백질", NutrientUnit.G));
        nutrientRepository.save(new Nutrient("당", NutrientUnit.G, carb));
    }

    @Test
    @Transactional
    void 새로운_음식을_등록한다() {
        // given
        Member member = new Member("이근우", "geunwoo.dev@gmail.com", "1", Role.USER);
        memberRepository.save(member);

        FoodNutrientCreateRequest carb = new FoodNutrientCreateRequest("탄수화물", 10);
        FoodNutrientCreateRequest fat = new FoodNutrientCreateRequest("지방", 5);
        FoodCreateRequest foodCreateRequest = new FoodCreateRequest(member.getId(), "파스타", "오뚜기",
                1, "g", "brand", 500, List.of(carb, fat));
        // when
        foodService.save(foodCreateRequest);

        // then
        assertDoesNotThrow(() ->
                foodRepository.getByName("파스타"));
        Food pasta = foodRepository.getByName("파스타");
        List<FoodNutrient> foodNutrients = foodNutrientRepository.getByFoodId(pasta.getId());
        assertThat(foodNutrients.size()).isEqualTo(2);
    }

    @Test
    @Transactional
    void 등록한_음식을_조회한다() {
        // given
        Member member = new Member("이근우", "geunwoo.dev@gmail.com", "1", Role.USER);
        memberRepository.save(member);

        Food pasta = new Food("파스타", member, 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500);
        Food pizza = new Food("피자", member, 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500);
        Food rice = new Food("밥", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500);
        foodRepository.save(pasta);
        foodRepository.save(pizza);
        foodRepository.save(rice);
        // when
        List<FoodResponse> foods = foodService.getFoodsByMemberId(member.getId());

        // then
        assertThat(foods.size()).isEqualTo(2);
    }


    @Test
    @Transactional
    void 음식의_영양성분_상세를_조회한다() {

        // given
        Member member = new Member("이근우", "geunwoo.dev@gmail.com", "1", Role.USER);
        memberRepository.save(member);

        Meal meal = new Meal(member, MealType.LUNCH);
        mealRepository.save(meal);

        Food pasta = new Food("파스타", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500);
        foodRepository.save(pasta);

        MealDetail mealDetail = new MealDetail(meal, pasta, 2);
        mealDetailRepository.save(mealDetail);

        Nutrient carb = nutrientRepository.getByName("탄수화물");
        Nutrient sugar = nutrientRepository.getByName("당");

        FoodNutrient foodNutrient1 = new FoodNutrient(pasta, carb, 50);
        FoodNutrient foodNutrient2 = new FoodNutrient(pasta, sugar, 15);
        foodNutrientRepository.save(foodNutrient1);
        foodNutrientRepository.save(foodNutrient2);
        // when
        NutrientDetailResponse response = foodService.getNutrientDetailByMealDetailId(mealDetail.getId());

        // then
        assertThat(response.getFoodName()).isEqualTo(pasta.getName());
        assertThat(response.getCalorieTotal()).isEqualTo((int) pasta.getCalorie() * mealDetail.getFoodCount());
        assertThat(response.getNutrientResponses().get(0).getName()).isEqualTo("탄수화물");
        assertThat(response.getNutrientResponses().get(0)
                .getChildNutrientResponses().get(0).getName()).isEqualTo("당");
    }

    @Test
    @Transactional
    void 검색_단어를_기준으로_자동완성_기능을_제공한다() {
        // given
        foodRepository.save(new Food("커피우유", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("커피", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("커피땅콩", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("콩커피", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("강커피콩", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("초코커피", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("파스타", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));

        // when
        List<SearchFoodResponse> response = foodService.getFoodNamesBySearchWord("커피");

        // then
        assertThat(response.size()).isEqualTo(6);
        assertThat(response.get(0).getName()).isEqualTo("커피");
        assertThat(response.get(5).getName()).isEqualTo("강커피콩");
    }

    @Test
    @Transactional
    void 검색_단어를_기준으로_관련된_음식을_조회하는_기능을_제공한다() {
        // given
        foodRepository.save(new Food("커피우유", 1, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("커피", 2, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("커피땅콩", 3, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("콩커피", 4, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("강커피콩", 5, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("초코커피", 6, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        foodRepository.save(new Food("파스타", 7, FoodUnit.G, "오뚜기", FoodType.from("brand"), 500));
        int pageStart = 0;

        // when
        List<FoodResponse> response = foodService.getFoodsByName("커피", pageStart);

        // then
        assertThat(response.size()).isEqualTo(6);
        assertThat(response.get(0).getName()).isEqualTo("커피");
        assertThat(response.get(5).getName()).isEqualTo("강커피콩");
        assertThat(response.get(5).getAmount()).isEqualTo(5);
    }

    @Test
    @Transactional
    void 등록한_음식을_삭제한다() {
        // given
        Member member = new Member("이근우", "geunwoo.dev@gmail.com", "1", Role.USER);
        memberRepository.save(member);

        FoodNutrientCreateRequest carb = new FoodNutrientCreateRequest("탄수화물", 10);
        FoodNutrientCreateRequest fat = new FoodNutrientCreateRequest("지방", 5);
        FoodCreateRequest foodCreateRequest = new FoodCreateRequest(member.getId(), "파스타", "오뚜기",
                1, "g", "brand", 500, List.of(carb, fat));
        FoodResponse response = foodService.save(foodCreateRequest);
        Long foodId = response.getId();

        // when
        foodService.deleteFoodAndFoodNutrientsById(foodId);

        // then
        assertThatThrownBy(() -> {
            foodRepository.getById(foodId);
        })
                .isInstanceOf(NoSuchFoodException.class)
                .hasMessage("존재하지 않는 음식입니다.");
        assertThat(foodNutrientRepository.getByFoodId(foodId).size()).isEqualTo(0);
    }
}
