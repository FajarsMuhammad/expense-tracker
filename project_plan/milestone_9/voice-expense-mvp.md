# Voice Expense MVP

## Overview
Voice Expense is an MVP feature that allows users to create expense transactions using voice input.
The system converts speech to text on the client side and processes the text on the backend using a rule-based NLP engine.

This MVP is designed for:
- Solo developer execution
- Fast iteration
- Minimal infrastructure cost
- Easy upgrade to AI-based NLP in the future

---

## MVP Goals
- Users can record expenses using voice
- Automatic extraction of:
    - Amount
    - Category
    - Wallet
- Transactions saved as standard expenses
- Manual confirmation if parsing confidence is low

---

## Non-Goals (Out of Scope)
- Custom speech model training
- Conversational AI
- Multi-language support (ID only)
- Multi-transaction parsing
- Income voice entry

---

## User Flow
1. User taps microphone button
2. User speaks an expense sentence
3. Browser converts speech to text
4. Text is sent to backend
5. Backend parses transaction
6. User confirms result
7. Transaction is saved

---

## High-Level Architecture

```mermaid
sequenceDiagram
    participant User
    participant Browser
    participant Backend
    participant Database

    User->>Browser: Speak expense
    Browser->>Browser: Speech to Text
    Browser->>Backend: POST /api/voice-expense
    Backend->>Backend: Parse text (rules)
    Backend->>Database: Save transaction
    Backend->>Browser: Parsed result
