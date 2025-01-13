# Spreadsheet System in Java

## Overview
This project implements a basic spreadsheet system in Java, similar to Excel but on a smaller scale. The spreadsheet supports:
- Basic mathematical operations
- Cell references
- Formula calculations
- Error handling for edge cases

## Key Features
### Cell Types Support
- **Numbers:** Integer and decimal numbers (e.g., `5`, `10.5`)
- **Text:** Any string that isn't a number or formula
- **Formulas:** Mathematical expressions starting with `=` (e.g., `=1+2`, `=A1+B2`)

### Formula Capabilities
- Arithmetic operations: `+`, `-`, `*`, `/`
- Cell references: (e.g., `A1`, `B2`)
- Nested parentheses
- Decimal number support
- **Error handling**:
  - `#REF!`: Circular references
  - `#ERR!`: Invalid formulas or syntax

### Core Functions
1. **Cell Type Detection:**
   - Identifies whether a cell contains text, numbers, or formulas.
   - Validates formula syntax and structure.

2. **Formula Evaluation:**
   - Computes expressions while handling dependencies.
   - Maintains order of operations (PEMDAS).

3. **Dependency Management:**
   - Tracks cell dependencies and detects circular references.
   - Ensures proper evaluation order.

## Implementation Details
### Key Classes
- `Ex2Sheet`: Main spreadsheet implementation.
- `SCell`: Handles individual cell logic.
- `CellEntry`: Converts cell coordinates (e.g., `A1` to `[0,0]`).

### Major Algorithms
- **Depth Calculation:** Prevents circular references and determines the computation order.
- **Formula Parsing:** Validates syntax, processes cell references, and handles nested expressions.
- **Error Handling:** Detects syntax errors, circular references, and invalid cells.

## Usage Example
```java
// Create a new spreadsheet
Ex2Sheet sheet = new Ex2Sheet(5, 5);  // 5x5 spreadsheet

// Set some values
sheet.set(0, 0, "5");           // A1 = 5
sheet.set(0, 1, "=A1+3");       // A2 = 8
sheet.set(1, 0, "=A1*2");       // B1 = 10
sheet.set(1, 1, "=A2+B1");      // B2 = 18

// Retrieve values
System.out.println(sheet.get(0, 1)); // Prints: 8
System.out.println(sheet.get(1, 1)); // Prints: 18
Error Handling
#ERR!: Invalid formulas or syntax errors.
#REF!: Circular references.
Empty cells return an empty string ("").
Development Process
Implemented basic cell structure and type detection.
Added formula parsing and validation.
Developed dependency tracking system.
Implemented formula evaluation.
Enhanced error handling for edge cases.
Optimized performance and code structure.
Testing
Comprehensive validation of:
Formula correctness
Mathematical operations
Cell references
Error conditions and edge cases
Future Improvements
Add support for advanced mathematical functions (e.g., SUM, AVG).
Improve error reporting with detailed explanations.
Optimize performance for larger spreadsheets.
Create a user-friendly graphical interface.
Author: Shira
Date: January 2025
Course: Introduction to Computer Science
