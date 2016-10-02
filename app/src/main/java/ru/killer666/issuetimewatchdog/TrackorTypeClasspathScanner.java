package ru.killer666.issuetimewatchdog;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class TrackorTypeClasspathScanner {
    List<Class<? extends TrackorType>> scan() {
        List<Class<? extends TrackorType>> result = Lists.newArrayList();

        result.add(Issue.class);
        result.add(TimeRecord.class);

        return result;
    }
}