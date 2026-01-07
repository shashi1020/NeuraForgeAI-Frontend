<img width="1584" height="672" alt="neuraforgeai banner" src="https://github.com/user-attachments/assets/13962f2d-f152-4674-a8c2-022cda4e4c53" />
# 🧠 NeuraForgeAi

### Unlock the Knowledge within your Documents

![NeuraForgeAi Banner](path/to/your/banner_image.png)

## 📖 About The Project

**NeuraForgeAi** is a cutting-edge native Android application that transforms static PDF documents into interactive conversations. By leveraging the power of Generative AI and Vector Search, users can upload documents, receive instant summaries, and ask complex questions to retrieve accurate answers based specifically on the document's content.

Built with a "Kotlin First" philosophy, this project demonstrates modern Android development practices, utilizing **Jetpack Compose** for UI, **Hilt** for dependency injection, and **Clean Architecture**.

## ✨ Key Features

* **📄 PDF Ingestion:** Seamlessly upload and parse local PDF files.
* **🤖 AI-Powered Summaries:** Get instant, high-quality executive summaries of long documents using **Google Gemini**.
* **💬 Context-Aware Q&A:** Chat with your PDF. The app uses vector embeddings to find the exact section of the text relevant to your question.
* **⚡ Real-time Analysis:** Fast processing and response times.
* **🎨 Modern UI:** A beautiful, dark-mode friendly interface built entirely with Jetpack Compose.

## 🛠️ Tech Stack & Architecture

NeuraForgeAi is built with modern Android standards and a robust backend integration.

### Android (Client)
* **Language:** [Kotlin](https://kotlinlang.org/) (100%)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Design)
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
* **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
* **Asynchronicity:** Kotlin Coroutines & Flow
* **Navigation:** Jetpack Compose Navigation

### AI & Backend Integration
* **LLM Model:** [Google Gemini API](https://ai.google.dev/) (for text generation and summarization).
* **Vector Database:** [pgvector](https://github.com/pgvector/pgvector) (PostgreSQL extension for vector similarity search).
* **Embeddings:** Text embeddings are generated and stored to allow semantic search over document chunks.

## 🚀 How It Works (Under the Hood)

1.  **Extraction:** The app extracts raw text from the uploaded PDF.
2.  **Chunking:** Text is split into manageable semantic chunks.
3.  **Embedding:** Each chunk is converted into a vector embedding.
4.  **Storage:** Embeddings are stored in a **pgvector** enabled database.
5.  **Retrieval:** When a user asks a question, the query is converted to a vector, compared against the database for cosine similarity, and the most relevant chunks are fed to the **Gemini API** to generate a context-aware answer.

## 📸 Screenshots

| Dashboard | Document Upload | AI Chat Interface |
|:---:|:---:|:---:|
| *(Place Screenshot Here)* | *(Place Screenshot Here)* | *(Place Screenshot Here)* |

## 🏁 Getting Started

### Prerequisites
* Android Studio Koala or newer.
* JDK 17+.
* A Google Gemini API Key.
* Access to a PostgreSQL instance with `pgvector` extension installed.

### Installation

1.  **Clone the repo**
    ```sh
    git clone [https://github.com/yourusername/NeuraForgeAi.git](https://github.com/yourusername/NeuraForgeAi.git)
    ```
2.  **Add API Keys**
    Create a `local.properties` file in the root directory and add:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    DB_CONNECTION_STRING=your_postgres_url
    ```
3.  **Build and Run**
    Sync Gradle project and run on an Emulator or Physical device.

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---


