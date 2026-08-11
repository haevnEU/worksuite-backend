package de.haevn.worksuite.cheatsheet;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheatSheetRepository extends MongoRepository<CheatSheetModel, UUID> {

    List<CheatSheetModel> findAllByCategoryIgnoreCase(String category);

    List<CheatSheetModel> findAllByCategoryIgnoreCaseAndSubcategoryIgnoreCase(String category, String subcategory);
}