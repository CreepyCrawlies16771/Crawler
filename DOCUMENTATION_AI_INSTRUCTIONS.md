# Documentation Writing Instructions for AI Assistants

This document guides future AI assistants on writing documentation for the Crawler robotics library.

## Core Principle

Crawler values simplicity over completeness. Documentation should be easy to understand, not cover every possible feature.

Write for new programmers who want to understand what Crawler does and how to use it quickly.

## What is Crawler

Crawler is a straightforward pathing library for FTC teams that:

- Is simple to understand
- Gets robots running in 30 minutes  
- Focuses on common tasks
- Is intentionally limited in scope

Documentation should reflect this philosophy.

## Documentation Structure

Documentation lives in two places:

### Markdown Files (Primary)
- Location: `/docs/` folder
- Format: GitHub Flavored Markdown
- Purpose: Main content
- Advantages: Easy to read, version controlled

### HTML Pages (Navigation)
- Location: `/docs-html/` folder
- Purpose: Landing page and navigation
- Approach: Keep minimal; link to markdown

**When adding new documentation:**
1. Write as markdown file in `/docs/`
2. Add link in `/docs-html/index.html`
3. Done!

## Writing Guidelines

### Tone and Language

Use simple, direct language:

- "Click the button" not "Engage the button"
- "The motor spins" not "exhibits rotational motion"
- Write for people new to programming
- Be encouraging but honest

### Structure

Every guide should have:

```markdown
# Title

Brief intro (1 sentence) of what this covers.

## What You'll Learn

- Point 1
- Point 2  
- Point 3

## Prerequisites

What you need:
- Item 1
- Item 2

## Steps

### Step 1: Title
Description and code

### Step 2: Title
Description and code

## Example Code

Complete working code

## Troubleshooting

Common problems and solutions

## What's Next

Link to related topics
```

### Code Examples

Code examples must be:

- Complete and working
- Properly commented
- Realistic
- Copy-paste ready

Example:

```java
// Complete example someone can use
public class MoveForward extends LinearOpMode {
    public void runOpMode() {
        DcMotor motor = hardwareMap.get(DcMotor.class, "motor");
        
        waitForStart();
        
        // Move at 50% power
        motor.setPower(0.5);
        sleep(1000);
        motor.stop();
    }
}
```

### What NOT to Do

- Don't promise features Crawler doesn't have
- Don't add unnecessary complexity
- Don't use emojis
- Don't write for advanced users
- Don't explain every Java detail
- Don't make pages too long

## Color Scheme (Dark Theme)

### Primary Colors
- **Background**: Dark gray (`#111827`)
- **Section**: Slightly lighter (`#1F2937`)
- **Text**: Light gray (`#E5E7EB`)
- **Muted Text**: Medium gray (`#9CA3AF`)

### Green Accents
- **Primary**: `#4ADE80` - Buttons, links, highlights
- **Secondary**: `#22C55E` - Hover states
- **Dark**: `#16A34A` - Text emphasis

### Semantic Colors
- **Success**: Green (`#4ADE80`)
- **Warning**: Orange (`#D97706`)
- **Error**: Red (`#DC2626`)
- **Info**: Blue (`#3B82F6`)

If writing HTML, use these CSS variables:
```css
--primary-green: #4ADE80;
--secondary-green: #22C55E;
--dark-green: #16A34A;
--text-light: #E5E7EB;
--text-muted: #9CA3AF;
--bg-primary: #1F2937;
--bg-secondary: #111827;
--bg-hover: #374151;
```

## Adding New Documentation

### To Add a New Markdown Guide

1. Create file in `/docs/your-topic.md`
2. Follow the structure above
3. Add this to `/docs-html/index.html` in the docs grid:

```html
<div class="doc-card">
    <h3>Your Topic</h3>
    <p>Brief description.</p>
    <a href="your-topic.html" class="link-green">Read</a>
</div>
```

### To Add a New Tutorial

1. Create file in `/docs/tutorial-name.md`
2. Follow tutorial structure
3. Add to tutorial list in `/docs-html/index.html`:

```html
<div class="tutorial-item">
    <span class="tutorial-number">X</span>
    <div class="tutorial-content">
        <h3>Tutorial Title</h3>
        <p>Brief description.</p>
        <a href="tutorial-name.html">Read Tutorial</a>
    </div>
</div>
```

### To Create HTML Page

Only if absolutely necessary. Use markdown when possible.

If you must:
1. Copy `/docs-html/TEMPLATE.html`
2. Update content
3. Link from main index
4. Use CSS classes from `style.css`

## Types of Documentation

### Getting Started
- Installation in 5 minutes
- First OpMode tutorial
- Basic hardware setup

### How-To Guides
- How to move a motor
- How to read a sensor
- How to write teleoperation code

### Concept Guides
- What is an OpMode?
- What is hardware mapping?
- How motors work

### Troubleshooting
- Build errors and solutions
- Runtime errors and solutions
- Common mistakes
- Debugging

## Checklist Before Finishing

- [ ] Clear title about what will be learned
- [ ] Simple language anyone understands
- [ ] No emojis
- [ ] Working code examples
- [ ] Dark theme compatible
- [ ] No broken links
- [ ] Markdown properly formatted
- [ ] No false feature promises
- [ ] Spellcheck done
- [ ] Less than 2000 words

## Common Mistakes to Avoid

**Too complicated:**
"Utilize the object-oriented paradigm to instantiate motor controller classes..."

**Better:**
"Set the motor power from -1.0 (full backward) to 1.0 (full forward)."

**Too long:**
5000-word guide on motor theory.

**Better:**
"Motors need power to spin. Set power from 0 (off) to 1.0 (full)."

**Overselling:**
"Use Crawler's advanced trajectory planner for optimal routes."

**Better:**
"Crawler helps you move your robot forward, backward, and turn."

## Template: Simple Tutorial

```markdown
# How to Move Your Motor

Move your robot forward with Crawler.

## What You'll Learn

- How to get a motor
- How to set motor power
- How to use sleep()

## Prerequisites

- FTC project with a motor named "motor"
- Basic Java knowledge

## Steps

### Step 1: Get the Motor

In your OpMode, get a reference to your motor:

```java
DcMotor motor = hardwareMap.get(DcMotor.class, "motor");
```

### Step 2: Set Power

Tell the motor how fast to spin:

```java
motor.setPower(0.5);  // 50% power
```

Power ranges from -1.0 to 1.0:
- -1.0: Full backward
- 0.0: Off
- 1.0: Full forward

### Step 3: Wait

Let the motor run:

```java
sleep(1000);  // Wait 1 second
```

### Step 4: Stop

Turn off the motor:

```java
motor.setPower(0);
```

## Complete Code

```java
public class MoveForward extends LinearOpMode {
    public void runOpMode() {
        DcMotor motor = hardwareMap.get(DcMotor.class, "motor");
        
        waitForStart();
        
        motor.setPower(0.5);
        sleep(1000);
        motor.setPower(0);
    }
}
```

## What's Next

- [How to Control with Gamepad](gamepad.md)
- [How to Read Encoders](encoders.md)
```

## Questions to Ask When Writing

- "Would someone new to robotics understand this?"
- "Is this the simplest way to explain it?"
- "Does this code actually work?"
- "Did I promise a feature Crawler doesn't have?"
- "Could this be shorter?"
- "Are there any emojis I need to remove?"

---

**Remember**: Good documentation helps people succeed quickly. Simplicity is the goal.

## Documentation Structure

### 1. User Guide (`docs/USER_GUIDE.md`)
The main entry point for new users. Should include:
- Quick overview of what Crawler does
- Installation steps
- Basic concepts explanation
- Links to tutorials
- FAQ section
- Support resources

### 2. Markdown Tutorials (`docs/*.md`)
Each tutorial should:
- Have a clear title describing what the user will learn
- Start with prerequisites
- Include step-by-step instructions
- Provide code examples with explanations
- End with "What's Next?" suggestions
- Be no longer than 5-10 minutes to read

### 3. HTML Documentation (`docs-html/`)
For visual documentation and interactive examples:
- Create responsive HTML pages
- Use the styling guidelines below
- Include working code examples
- Add screenshots or diagrams where helpful

## Writing Guidelines

### Tone and Style

- **Use simple, direct language**: Avoid jargon when possible; explain technical terms
- **Be encouraging**: New users might feel intimidated by robotics
- **Use "you/your"**: "You can do this" not "One can do this"
- **Be concise**: Get to the point quickly
- **Use active voice**: "Click the button" not "The button should be clicked"

### Structure

Every tutorial/guide should have:

```markdown
# Title: What You'll Learn

## Overview
Brief explanation (1-2 sentences) of what this covers.

## Prerequisites
What the user needs before starting.

## Learning Objectives
What skills the user will gain.

## Main Content
- Use numbered steps for procedural content
- Use bullet points for lists
- Use code blocks with syntax highlighting
- Include images/diagrams for complex concepts

## Example Code
Working, tested code snippets

## Common Issues
Troubleshooting section

## Next Steps
What to learn next

## Additional Resources
Links to related documentation
```

### Code Examples

- All code should be copy-paste ready
- Include comments explaining what the code does
- Show both correct and common incorrect versions when helpful
- Use realistic examples from the Crawler library

Example:

```java
// ✓ CORRECT: Clear, readable motor control
DcMotor frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
frontLeft.setPower(0.5);  // 50% power

// ✗ AVOID: Unclear or incomplete
motor.setPower(1);
```

## Color Scheme Guidelines

### Primary Colors
- **Background**: White (`#FFFFFF`) or very light gray (`#F8F9FA`)
- **Text**: Dark gray (`#2C3E50`) for readability

### Accent Colors (Green Theme)
- **Primary Green**: `#27AE60` - For buttons, links, important highlights
- **Secondary Green**: `#16A085` - For hover states, secondary elements
- **Light Green**: `#D5F4E6` - For backgrounds, callout boxes
- **Dark Green**: `#1E8449` - For text emphasis, code syntax

### Semantic Colors
- **Success/Positive**: Use Primary Green (`#27AE60`)
- **Warning**: Use Orange (`#E67E22`)
- **Error/Danger**: Use Red (`#C0392B`)
- **Info/Note**: Use Light Blue (`#D6EAF8`)

### CSS Example
```css
:root {
  --primary-green: #27AE60;
  --secondary-green: #16A085;
  --light-green: #D5F4E6;
  --dark-green: #1E8449;
  --text-dark: #2C3E50;
  --bg-light: #F8F9FA;
  --bg-white: #FFFFFF;
}

button {
  background-color: var(--primary-green);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: var(--secondary-green);
}

.callout {
  background-color: var(--light-green);
  border-left: 4px solid var(--primary-green);
  padding: 15px;
  margin: 15px 0;
  border-radius: 4px;
}

.success { color: var(--primary-green); }
.warning { color: #E67E22; }
.error { color: #C0392B; }
.info { color: #3498DB; }
```

## HTML Structure

When creating HTML documentation:

1. **Folder Organization**
   ```
   docs-html/
   ├── index.html           # Landing page
   ├── css/
   │   └── style.css        # Main stylesheet
   ├── js/
   │   └── scripts.js       # Interactive features
   └── assets/
       ├── images/          # Screenshots, diagrams
       └── code-examples/   # Example files
   ```

2. **Base HTML Template**
   ```html
   <!DOCTYPE html>
   <html lang="en">
   <head>
       <meta charset="UTF-8">
       <meta name="viewport" content="width=device-width, initial-scale=1.0">
       <title>Page Title - Crawler Library</title>
       <link rel="stylesheet" href="../css/style.css">
   </head>
   <body>
       <header>
           <nav><!-- Navigation --></nav>
       </header>
       <main>
           <article>
               <!-- Content -->
           </article>
       </main>
       <footer>
           <!-- Footer content -->
       </footer>
       <script src="../js/scripts.js"></script>
   </body>
   </html>
   ```

3. **Responsive Design**
   - Mobile-first approach
   - All content readable on phones, tablets, desktops
   - Touch-friendly buttons (min 44px height)
   - Clear, readable font sizes (16px minimum for body text)

## Types of Documentation to Create

### 1. Getting Started
- Installation guide
- First OpMode tutorial
- Basic hardware setup
- "Hello World" for robots

### 2. How-To Guides
- "How to create autonomous routines"
- "How to integrate vision"
- "How to use the dashboard"
- "How to tune your robot"

### 3. Concept Guides
- OpMode types explained
- Coordinate systems
- Motor control basics
- Sensor types

### 4. Troubleshooting
- Common errors and solutions
- Build issues
- Runtime problems
- Performance optimization

### 5. Examples
- Complete working OpModes
- Real robot configurations
- Competition-tested code

## Checklist Before Publishing

- [ ] Title clearly describes what user will learn
- [ ] Uses simple, non-technical language
- [ ] Includes step-by-step instructions
- [ ] Has working code examples
- [ ] Tested on both markdown and HTML rendering
- [ ] Includes troubleshooting section
- [ ] Links to related documents
- [ ] No dead links
- [ ] Appropriate use of green accent colors
- [ ] Mobile-responsive (if HTML)
- [ ] Spellcheck completed
- [ ] Screenshots/diagrams are clear and labeled

## Tools and References

### Markdown
- Use standard GitHub Flavored Markdown (GFM)
- Include syntax highlighting with language tags

### HTML & CSS
- Modern CSS Grid/Flexbox for layouts
- No JavaScript frameworks required
- Progressive enhancement (works without JS)

### Writing Tools
- Grammarly or similar for spell-check
- Hemingway Editor for clarity
- Read-it-aloud tools for flow

## Examples in This Repository

Refer to existing documentation:
- [USER_GUIDE.md](USER_GUIDE.md) - Main user guide example
- [docs/setup.md](docs/setup.md) - Tutorial format
- [docs/first-auto.md](docs/first-auto.md) - Getting started example

Use these as templates for new documentation.

## When to Update Documentation

Update documentation when:
- Library features change
- Users report confusion in GitHub issues
- New tutorials are needed
- Examples become outdated
- Build process changes
- New hardware is supported

## Feedback Loop

If users ask the same questions repeatedly:
1. Document the answer in the appropriate guide
2. Link to it from FAQ and index
3. Update related tutorials

---

**Remember**: The best documentation is documentation that helps users succeed! 🎯

If you're unsure, ask: "Would a beginner understand this?"
