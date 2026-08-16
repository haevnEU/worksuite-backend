package de.haevn.worksuite.retro;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RetroRepository extends MongoRepository<Retro, UUID> {

}