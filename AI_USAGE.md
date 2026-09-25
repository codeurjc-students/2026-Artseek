# AI Tool Usage Log

This document records the use of artificial intelligence tools during the conception and development of Artseek. Related interactions are grouped by purpose so that the work remains traceable without reproducing every individual prompt.

AI-generated material is treated as a design and development aid. All proposals are reviewed, directed, and validated by the student before being incorporated into the project.

## AI-2026-09-12-001 — Application screen drafts and visual iteration

- **Date or period:** 12–16 September 2026.
- **Phase:** UI/UX design and visual prototyping.
- **Objective:** Create the first drafts of the application's screens from a landing-page draft previously designed by the student in Figma, while maintaining a consistent visual identity and translating the product concept into a coherent set of interfaces.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** The student supplied the application description, the colour palette, the self-made Figma landing-page draft, and instructions describing how the remaining screens had been conceived and how they were expected to look and behave.
- **How it was used:** Codex was asked to produce screen drafts based on the supplied visual reference and product requirements. The work followed an iterative, screen-by-screen process: each proposal was reviewed by the student, who requested changes to details, layout, styling, and overall appearance. Codex then revised the draft until the expected result was achieved.
- **Complementary tools and resources:** Figma was used by the student to create the original landing-page draft that served as the primary visual reference. No additional plugins, skills, MCP servers, or external connectors are recorded for this use.
- **Result and student review:** A consistent set of application screen drafts was produced. The student directed the iterations, selected the accepted visual solutions, and validated the final appearance of each screen.

## AI-2026-11-19-001 — Role-aware screen navigation diagram

- **Date:** 19 November 2026.
- **Phase:** Analysis and project documentation.
- **Objective:** Create a screen navigation diagram for inclusion in `README.md` that represented the relationships between the application's screens and the permissions required to follow each navigation path.
- **Tool:** OpenAI Codex, desktop application.
- **Specific version/model:** GPT-5.6 Sol.
- **Configuration:** `medium` reasoning level, agentic Default mode, interactive chat workflow.
- **Context provided by the student:** Codex was instructed to derive the navigation structure and access rules from the content of `README.md`, especially Section 7, **Analysis**, including **Screens and navigation**, **Application Screens Drafts**, and **User permissions**. The screen mock-ups stored in `docs/images/` were also provided as the visual source material for the diagram.
- **How it was used:** Codex analysed the documented screens, the transitions between them, and the permissions assigned to anonymous, registered, artist, owner, and administrator contexts. It then organized the screens into a role-aware navigation flow and represented the minimum permission required for each transition through distinct arrow styles and colours.
- **Complementary tools and resources:** Local repository inspection and direct SVG generation were used. The existing PNG screen drafts in `docs/images/` were embedded as visual references in the diagram. No external plugins, MCP servers, or connectors are recorded for this use.
- **Result and student review:** The resulting diagram was saved as `docs/navigation-diagram.svg` and linked from the **Navigation Diagram** subsection of `README.md`. It depicts both the application's screen navigation and the access permissions associated with its transitions.
