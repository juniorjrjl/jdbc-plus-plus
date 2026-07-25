package br.com.jdbcpp.processor.service.parameter;

import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.SimpleParamInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamPathExtractor {

    public ParamPathExtractor() {}

    public Map<String, List<ParamInfo>> build(final ClassParamInfo root) {
        final Map<String, List<ParamInfo>> paths = new HashMap<>();
        visit(root, new ArrayList<>(List.of(root)), paths);
        return paths;
    }

    private void visit(final ParamInfo current,
                       final List<ParamInfo> currentPath,
                       final Map<String, List<ParamInfo>> paths) {

        switch (current) {

            case SimpleParamInfo simple when (simple.isIgnore()) -> {}

            case SimpleParamInfo simple  ->
                paths.put(simple.getName(), List.copyOf(currentPath));

            case ClassParamInfo clazz -> {
                for (final var nested : clazz.getNestedProperties()) {
                    final List<ParamInfo> nextPath = new ArrayList<>(currentPath);
                    nextPath.add(nested);
                    visit(nested, nextPath, paths);
                }
            }
        }
    }
}