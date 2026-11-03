package resumeanalyzer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CoreResumeAnalyzer {

    private static final String JOB_DESCRIPTION = "Senior Java Developer with Spring Boot, Microservices, and SQL expertise.";
    private static final List<String> KEYWORDS = Arrays.asList(
        "java", "spring boot", "microservices", "sql", "rest api", "hibernate", "maven"
    );

    // ── Job Recommendation Rules ──────────────────────────────────────────────
    // Each entry: { roleTitle, required keywords (must have ALL), bonus keywords }
    private static final List<Object[]> JOB_ROLES = Arrays.asList(
        new Object[]{"Senior Java Developer",
            Arrays.asList("java", "spring boot", "microservices", "sql"),
            Arrays.asList("hibernate", "maven", "rest api", "docker", "kafka")},
        new Object[]{"Backend Developer",
            Arrays.asList("java", "sql", "rest api"),
            Arrays.asList("spring boot", "hibernate", "maven", "node")},
        new Object[]{"Full Stack Developer",
            Arrays.asList("java", "sql", "rest api"),
            Arrays.asList("react", "javascript", "spring boot", "html", "css")},
        new Object[]{"Data Engineer",
            Arrays.asList("sql", "python"),
            Arrays.asList("spark", "hadoop", "kafka", "airflow", "etl")},
        new Object[]{"DevOps Engineer",
            Arrays.asList("docker", "kubernetes"),
            Arrays.asList("jenkins", "aws", "ci/cd", "terraform", "linux")},
        new Object[]{"Cloud Developer",
            Arrays.asList("aws", "docker"),
            Arrays.asList("kubernetes", "lambda", "s3", "microservices", "terraform")},
        new Object[]{"Frontend Developer",
            Arrays.asList("react", "javascript"),
            Arrays.asList("html", "css", "typescript", "redux", "next.js")}
    );

    static class Candidate {
        String name;
        double score;
        String summary;
        List<String> matchedKeywords;
        List<String> missingKeywords;
        List<String> suggestions;
        List<String> jobRecommendations;

        Candidate(String name, double score, String summary,
                  List<String> matched, List<String> missing,
                  List<String> suggestions, List<String> jobRecommendations) {
            this.name = name;
            this.score = score;
            this.summary = summary;
            this.matchedKeywords = matched;
            this.missingKeywords = missing;
            this.suggestions = suggestions;
            this.jobRecommendations = jobRecommendations;
        }
    }

    public static String getAnalysisResults(File[] resumeFiles, String jobDescription) {
        if (resumeFiles == null || resumeFiles.length == 0) {
            return "No files found. Please ensure PDF files are selected.";
        }

        StringBuilder report = new StringBuilder();
        report.append("==========================================\n");
        report.append("🎯 TARGET JOB DESCRIPTION:\n   ").append(JOB_DESCRIPTION).append("\n");
        report.append("==========================================\n\n");

        List<Candidate> candidates = new ArrayList<>();

        for (File file : resumeFiles) {
            try {
                String text = extractText(file);
                String lower = text.toLowerCase();

                double score                    = calculateScore(lower);
                String summary                  = summarize(text);
                List<String> matched            = getMatchedKeywords(lower);
                List<String> missing            = getMissingKeywords(lower);
                List<String> suggestions        = generateSuggestions(lower, missing);
                List<String> recommendations    = recommendJobs(lower);

                candidates.add(new Candidate(file.getName(), score, summary,
                        matched, missing, suggestions, recommendations));

            } catch (IOException e) {
                report.append("❌ Error reading: ").append(file.getName()).append("\n");
            }
        }

        // Rank highest score first
        candidates.sort((c1, c2) -> Double.compare(c2.score, c1.score));

        for (int i = 0; i < candidates.size(); i++) {
            Candidate c = candidates.get(i);

            report.append(String.format("RANK #%d: %s\n", i + 1, c.name.toUpperCase()));
            report.append(String.format("📊 MATCH SCORE: %.2f%%\n", c.score));

            // Summary
            report.append("📝 PROFILE SUMMARY:\n   ").append(c.summary).append("\n\n");

            /* Matched keywords
            report.append("✅ MATCHED KEYWORDS:\n   ");
            report.append(c.matchedKeywords.isEmpty() ? "None" : String.join(", ", c.matchedKeywords));
            report.append("\n\n");
*/
            /* Missing keywords
            report.append("❌ MISSING KEYWORDS:\n   ");
            report.append(c.missingKeywords.isEmpty() ? "None — great coverage!" : String.join(", ", c.missingKeywords));
            report.append("\n\n");
*/
            // Suggestions
            report.append("💡 SUGGESTIONS FOR IMPROVEMENT:\n");
            c.suggestions.forEach(s -> report.append("   • ").append(s).append("\n"));
            report.append("\n");

            // Job Recommendations
            report.append("🚀 RECOMMENDED JOB ROLES:\n");
            c.jobRecommendations.forEach(j -> report.append("   → ").append(j).append("\n"));
            report.append("\n");

            report.append("------------------------------------------\n\n");
        }

        return report.toString();
    }

    // ── Scoring ───────────────────────────────────────────────────────────────
    private static double calculateScore(String text) {
        long matches = KEYWORDS.stream().filter(text::contains).count();
        return ((double) matches / KEYWORDS.size()) * 100;
    }

    private static List<String> getMatchedKeywords(String text) {
        return KEYWORDS.stream().filter(text::contains).collect(Collectors.toList());
    }

    private static List<String> getMissingKeywords(String text) {
        return KEYWORDS.stream().filter(k -> !text.contains(k)).collect(Collectors.toList());
    }

    // ── Suggestions ───────────────────────────────────────────────────────────
    private static List<String> generateSuggestions(String text, List<String> missing) {
        List<String> suggestions = new ArrayList<>();

        // Keyword-based suggestions
        Map<String, String> keywordAdvice = new LinkedHashMap<>();
        keywordAdvice.put("spring boot",    "Add Spring Boot projects or certifications to strengthen backend credibility.");
        keywordAdvice.put("microservices",  "Mention microservices experience — even decomposing a monolith counts.");
        keywordAdvice.put("sql",            "Include SQL skills: query optimization, stored procedures, or schema design.");
        keywordAdvice.put("hibernate",      "Add Hibernate/JPA usage under your Java experience.");
        keywordAdvice.put("maven",          "List build tools like Maven or Gradle in your skills section.");
        keywordAdvice.put("rest api",       "Highlight REST API design/consumption in your project descriptions.");
        keywordAdvice.put("java",           "Java is the core requirement — make sure it appears prominently.");

        for (String kw : missing) {
            if (keywordAdvice.containsKey(kw)) {
                suggestions.add(keywordAdvice.get(kw));
            }
        }

        // General structural suggestions
        if (!text.contains("github") && !text.contains("gitlab")) {
            suggestions.add("Add a GitHub/GitLab profile link to showcase your actual code.");
        }
        if (!text.contains("certified") && !text.contains("certification")) {
            suggestions.add("Consider adding relevant certifications (e.g., Oracle Java, AWS, Spring Professional).");
        }
        if (!text.contains("team") && !text.contains("led") && !text.contains("mentored")) {
            suggestions.add("Include leadership or collaboration experience — teams value it highly.");
        }
        if (text.length() < 1500) {
            suggestions.add("Resume seems thin. Expand project descriptions with impact metrics (e.g., 'reduced latency by 30%').");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Strong resume! Consider tailoring the summary line to mirror the exact job title.");
        }

        return suggestions;
    }

    // ── Job Recommendations ───────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private static List<String> recommendJobs(String text) {
        List<String> recommendations = new ArrayList<>();

        for (Object[] role : JOB_ROLES) {
            String title               = (String) role[0];
            List<String> required      = (List<String>) role[1];
            List<String> bonus         = (List<String>) role[2];

            // Must match ALL required keywords
            boolean qualifies = required.stream().allMatch(text::contains);
            if (!qualifies) continue;

            // Count bonus keyword matches for confidence label
            long bonusMatches = bonus.stream().filter(text::contains).count();
            String confidence;
            if (bonusMatches >= bonus.size() * 0.6) {
                confidence = "Strong Match";
            } else if (bonusMatches >= bonus.size() * 0.3) {
                confidence = "Good Match";
            } else {
                confidence = "Potential Match";
            }

            recommendations.add(String.format("%s  [%s]", title, confidence));
        }

        if (recommendations.isEmpty()) {
            recommendations.add("No strong role matches found. Broaden your skill keywords to unlock recommendations.");
        }

        return recommendations;
    }

    // ── Summarizer ────────────────────────────────────────────────────────────
    private static String summarize(String text) {
        String[] lines = text.split("\\r?\\n");
        return Arrays.stream(lines)
                .filter(line -> line.trim().length() > 20)
                .limit(3)
                .collect(Collectors.joining(" ")) + "...";
    }

    // ── PDF Extractor ─────────────────────────────────────────────────────────
    private static String extractText(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("Extracted text length: " + text.length());
            return text;
        }
    }