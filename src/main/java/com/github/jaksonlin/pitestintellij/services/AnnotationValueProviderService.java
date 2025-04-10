package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.annotations.ValueProvider;
import com.github.jaksonlin.pitestintellij.annotations.ValueProviderType;
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext;
import com.github.jaksonlin.pitestintellij.util.GitUtil;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class AnnotationValueProviderService {
    private final Project project;

    public AnnotationValueProviderService(Project project) {
        this.project = project;
    }

    @Nullable
    public Object provideValue(@NotNull ValueProvider provider, @NotNull CaseCheckContext context) {
        ValueProviderType type = provider.getType();
        if (type != null) {
            switch (type) {
                case GIT_AUTHOR:
                    return getGitAuthor();
                case LAST_MODIFIER_AUTHOR:
                    return getLastModifierAuthor(context.getPsiMethod());
                case LAST_MODIFIER_TIME:
                    return getLastModifierTime(context.getPsiMethod());
                case CURRENT_DATE:
                    return getCurrentDate(provider.getFormat());
                case METHOD_NAME_BASED:
                    return generateDescription(context.getPsiMethod());
                case FIXED_VALUE:
                    return provider.getValue();
                case CLASS_NAME:
                    return guessClassUnderTestClassName(context.getPsiClass());
                case METHOD_NAME:
                    return guessMethodUnderTestMethodName(context.getPsiMethod());
                case METHOD_SIGNATURE:
                    String signature = tryGetMethodUnderTestSignature(context.getPsiClass(), context.getPsiMethod());
                    return signature != null ? signature : "";
                case FIRST_CREATOR_AUTHOR:
                    return getFirstCreatorAuthor(context.getPsiMethod());
                case FIRST_CREATOR_TIME:
                    return getFirstCreatorTime(context.getPsiMethod());
                default:
                    return null;
            }
        }
        return null;
    }

    private String getGitAuthor() {
        return Objects.toString(GitUtil.getGitUserInfo(project));
    }

    private String getLastModifierAuthor(@NotNull PsiMethod psiMethod) {
        Object lastModifyInfo = GitUtil.getLastModifyInfo(project, psiMethod);
        return lastModifyInfo != null ? Objects.toString(lastModifyInfo) : getGitAuthor();
    }

    private String getLastModifierTime(@NotNull PsiMethod psiMethod) {
        Long timestamp = GitUtil.getLastModifyInfo(project, psiMethod).getTimestamp();
        timestamp = timestamp != null ? timestamp * 1000 : null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(timestamp != null ? timestamp : System.currentTimeMillis()));
    }

    private String getCurrentDate(@Nullable String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format != null ? format : "yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private String generateDescription(@NotNull PsiMethod psiMethod) {
        String methodName = psiMethod.getName();
        Pattern pattern = Pattern.compile("([a-z])([A-Z])");
        Matcher matcher = pattern.matcher(methodName);
        String spacedName = matcher.replaceAll("$1 $2").replace("_", " ");
        return capitalizeFirst(spacedName.toLowerCase());
    }

    private String guessClassUnderTestClassName(@NotNull PsiClass psiClass) {
        String className = psiClass.getName();
        String baseClassName = (className != null ? className.replaceFirst("^Test", "").replaceFirst("Test$", "") : "");
        String qualifiedName = psiClass.getQualifiedName();
        String testPackage = "";

        if (qualifiedName != null) {
            int lastDotIndex = qualifiedName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                testPackage = qualifiedName.substring(0, lastDotIndex);
            }

            // Adjust package name based on common test package conventions
            testPackage = testPackage.replace(".test", "").replace(".tests", "");
            if (testPackage.endsWith(".")) {
                testPackage = testPackage.substring(0, testPackage.length() - 1);
            }
        }

        return (testPackage.isEmpty() ? "" : testPackage + ".") + baseClassName;
    }

    private String guessMethodUnderTestMethodName(@NotNull PsiMethod psiMethod) {
        return psiMethod.getName()
                .replaceFirst("^test", "")
                .replaceFirst("^should", "")
                .replaceFirst("^testShould", "")
                .split("_")[0]
                .toLowerCase();
    }

    @Nullable
    private String tryGetMethodUnderTestSignature(@NotNull PsiClass psiClass, @NotNull PsiMethod psiMethod) {
        String guessedClassName = guessClassUnderTestClassName(psiClass);
        String guessedMethodName = guessMethodUnderTestMethodName(psiMethod).toLowerCase();

        Project project = psiClass.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);

        PsiClass classUnderTest = psiFacade.findClass(guessedClassName, GlobalSearchScope.projectScope(project));
        if (classUnderTest == null) {
            return null;
        }
        // Check if the class under test has test methods, if this is the test class, return null
        boolean hasTestMethods = Arrays.stream(classUnderTest.getMethods())
                .anyMatch(method -> Arrays.stream(method.getAnnotations())
                        .anyMatch(annotation -> {
                            String annotationName = annotation.getQualifiedName();
                            return Objects.equals(annotationName, "org.junit.jupiter.api.Test") ||
                                    Objects.equals(annotationName, "org.junit.Test") ||
                                    (annotationName != null && annotationName.contains("Test"));
                        }));

        if (hasTestMethods) {
            return null;
        }

        PsiMethod methodUnderTest = Arrays.stream(classUnderTest.getMethods())
                .filter(method -> method.getName().toLowerCase().contains(guessedMethodName))
                .findFirst()
                .orElse(null);

        return methodUnderTest != null ? getMethodSignature(methodUnderTest) : null;
    }

    private String getMethodSignature(@NotNull PsiMethod psiMethod) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(psiMethod.getModifierList().getText().trim().split(" "))
                .filter(s -> !s.isEmpty() && !s.startsWith("@"))
                .collect(Collectors.joining(" ", sb, " "));

        if (!psiMethod.isConstructor()) {
            sb.append(Objects.requireNonNull(psiMethod.getReturnType()).getPresentableText()).append(" ");
        }

        sb.append(psiMethod.getName()).append("(");
        sb.append(Arrays.stream(psiMethod.getParameterList().getParameters())
                .map(param -> param.getType().getPresentableText() + " " + param.getName())
                .collect(Collectors.joining(", ")));
        sb.append(")");

        PsiClassType[] throwsList = psiMethod.getThrowsList().getReferencedTypes();
        if (throwsList.length > 0) {
            sb.append(" throws ");
            sb.append(Arrays.stream(throwsList)
                    .map(PsiClassType::getPresentableText)
                    .collect(Collectors.joining(", ")));
        }
        return sb.toString().replace("\n", " ").replaceAll("\\s+", " ").trim();
    }

    private String getFirstCreatorAuthor(@NotNull PsiMethod psiMethod) {
        Object firstCreatorInfo = GitUtil.getFirstCreatorInfo(project, psiMethod);
        return firstCreatorInfo != null ? firstCreatorInfo.toString() : getGitAuthor();
    }

    private String getFirstCreatorTime(@NotNull PsiMethod psiMethod) {
        Long timestamp = GitUtil.getFirstCreatorInfo(project, psiMethod).getTimestamp();
        timestamp = timestamp != null ? timestamp * 1000 : null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(timestamp != null ? timestamp : System.currentTimeMillis()));
    }

    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
