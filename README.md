Backend: https://github.com/shashi1020/NeuraForgeAI-Backend

NeuraForgeAI is a robust FastAPI-based backend designed for **PDF understanding**, **document chunking**, **embedding-based retrieval**, and **chat over documents using RAG (Retrieval-Augmented Generation)**. It extracts PDF text, generates a concise summary, stores chunks with embeddings in PostgreSQL, and allows users to chat with grounded, citation-based answers using Gemini.

---

## 🚀 Features

* **Upload & Analyze PDFs**

  * Extracts per-page text using PyPDF2
  * Creates smart overlapping chunks
  * Generates vector embeddings using Sentence Transformers (MPNet)
  * Stores chunks & metadata in PostgreSQL
  * Auto-extracts email, phone, and name (resume mode)
  * Detects document headings/topics
  * Generates summary using Gemini

* **Chat Over Documents**

  * Embeds the question
  * Performs similarity search over document chunks
  * Sends only relevant excerpts to Gemini
  * Returns grounded answers + source citations

* **Tech Stack**

  * **FastAPI**
  * **PostgreSQL** (with array embedding fields)
  * **SQLAlchemy**
  * **Sentence-Transformers** (MPNet)
  * **Gemini 2.5 Flash** API

---


