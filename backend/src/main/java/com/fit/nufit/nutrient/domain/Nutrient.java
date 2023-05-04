package com.fit.nufit.nutrient.domain;

import com.fit.nufit.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Nutrient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nutrient_id")
    private Long id;

    @Comment("상위 영양소 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_nutrient_id", referencedColumnName = "nutrient_id")
    private Nutrient parentNutrient;

    @Comment("하위 영양소 ID")
    @OneToMany(mappedBy = "parentNutrient")
    private final List<Nutrient> childNutrients = new ArrayList<>();

    @Comment("영양소 이름")
    @Column(name = "nutrient_name")
    private String name;

    @Comment("영양소 칼로리")
    @Column(name = "nutrient_calorie")
    private int calorie;

    @Comment("영양소 단위")
    @Enumerated(value = EnumType.STRING)
    @Column(name = "nutrient_unit", nullable = false)
    private NutrientUnit unit;

    public Nutrient(String name, int calorie, NutrientUnit unit) {
        this.name = name;
        this.calorie = calorie;
        this.unit = unit;
    }
}
