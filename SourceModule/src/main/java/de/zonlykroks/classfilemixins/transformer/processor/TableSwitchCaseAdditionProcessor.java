package de.zonlykroks.classfilemixins.transformer.processor;

import de.zonlykroks.classfilemixins.annotations.TableSwitchCaseAddition;
import de.zonlykroks.classfilemixins.transformer.util.TransformerUtils;
import java.lang.classfile.*;
import java.lang.classfile.instruction.*;

import java.util.ArrayList;
import java.util.List;

public class TableSwitchCaseAdditionProcessor extends AbstractAnnotationProcessor<TableSwitchCaseAddition> {
    @Override
    public ClassModel processAnnotation(TableSwitchCaseAddition annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule) {
        byte[] modified = transform(targetModel, getTransformingMethodBodies(annotation.method(), new CodeTransform() {
            int currentSwitchIndex = 0;

            @Override
            public void accept(CodeBuilder codeBuilder, CodeElement codeElement) {
                System.out.println(codeElement);
                if (codeElement instanceof TableSwitchInstruction tableSwitchInstruction) {
                    int low = tableSwitchInstruction.lowValue();
                    int high = tableSwitchInstruction.highValue();

                    List<SwitchCase> cases = new ArrayList<>();

                    if (currentSwitchIndex == annotation.switchIndex()) {
                        tableSwitchInstruction.cases().forEach(switchCase -> {
                            if(switchCase.caseValue() == annotation.caseValue()) {
                                System.out.println("Case value already exists in switch statement,replacing");
                            }else {
                                cases.add(switchCase);
                            }
                        });

                        List<SwitchCase> updatedCases = new ArrayList<>(cases);

                        Label newCaseLabel = codeBuilder.newLabel();
                        updatedCases.add(SwitchCase.of(annotation.caseValue(), newCaseLabel));

                        codeBuilder.tableswitch(
                                low,
                                high,
                                tableSwitchInstruction.defaultTarget(),
                                updatedCases
                        );

                        codeBuilder.labelBinding(newCaseLabel);
                        TransformerUtils.invokeVirtualSourceMethod(codeBuilder, targetModel, sourceMethodModule);
                        codeBuilder.return_();
                    } else {
                        codeBuilder.with(codeElement);
                        currentSwitchIndex++;
                    }
                    return;
                }

                codeBuilder.with(codeElement);
            }
        }));

        return parse(modified);
    }
}
