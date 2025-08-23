package com.pratham.finvera.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pratham.finvera.entity.Category;
import com.pratham.finvera.entity.SubCategory;
import com.pratham.finvera.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initCategories() {
        if (categoryRepository.count() > 0)
            return;

        addCategory("Food & Drinks", Arrays.asList(
                "Bar, cafe", "Groceries", "Restaurant, fast-food"));

        addCategory("Shopping", Arrays.asList(
                "Clothes & shoes", "Drug-store, chemist", "Electronics, accessories",
                "Free time", "Gifts, joy", "Health and beauty", "Home, garden",
                "Jewels, accessories", "Kids", "Pets, animals", "Stationery, tools"));

        addCategory("Housing", Arrays.asList(
                "Energy, utilities", "Maintenance, repairs", "Mortgage",
                "Property insurance", "Rent", "Services"));

        addCategory("Transportation", Arrays.asList(
                "Business trips", "Long distance", "Public transport", "Taxi"));

        addCategory("Vehicle", Arrays.asList(
                "Fuel", "Leasing", "Parking", "Rentals", "Vehicle insurance", "Vehicle maintenance"));

        addCategory("Life and entertainment", Arrays.asList(
                "Active sport, fitness", "Alcohol, tobacco", "Books, audio, subscriptions",
                "Charity, gifts", "Culture, sport events", "Education, development",
                "Healthcare, doctor", "Hobbies", "Holiday, trips, hotels", "Life events",
                "Lottery, gambling", "TV, streaming", "Wellness, beauty"));

        addCategory("Communication, PC", Arrays.asList(
                "Internet", "Phone, cell phone", "Postal services", "Software, apps, games"));

        addCategory("Financial expenses", Arrays.asList(
                "Advisory", "Charges, fees", "Child support", "Fines",
                "Insurances", "Loan, interests", "Taxes"));

        addCategory("Investments", Arrays.asList(
                "Collections", "Financial investments", "Realty", "Savings", "Vehicles, chattels"));

        addCategory("Income", Arrays.asList(
                "Checks, coupons", "Child support", "Dues & grants", "Gifts",
                "Interests, dividends", "Lending, renting", "Lottery, gambling",
                "Refunds (tax, purchase)", "Sale", "Wage, invoices"));

        addCategory("Others", Arrays.asList("Missing"));

    }

    private void addCategory(String categoryName, List<String> subCategoryNames) {
        Category category = Category.builder().name(categoryName).build();

        List<SubCategory> subCategories = subCategoryNames.stream()
                .map(name -> SubCategory.builder().name(name).category(category).build())
                .toList();

        category.setSubCategories(subCategories);
        categoryRepository.save(category);
    }
}
