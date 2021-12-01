package org.zfin.alliancegenome;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import si.mazi.rescu.serialization.jackson.DefaultJacksonObjectMapperFactory;

class JacksonObjectMapperFactoryZFIN extends DefaultJacksonObjectMapperFactory {

    protected ObjectMapper createInstance() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}