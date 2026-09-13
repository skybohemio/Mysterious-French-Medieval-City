# Bourges Audio Guide - Administrative Backend (ASP.NET Core)

This is a modern, fully featured administrative CMS backend designed for the management of points of interest (sites) and tour routes of the Bourges audio guide application.

Built using **C#**, **ASP.NET Core MVC (8.0)**, and **Entity Framework Core with SQLite**, it provides a polished web interface for adding, updating, and translating tourist catalog data, as well as syncing it to the Android client application database.

---

## Key Features

1. **Dashboard Overview**: Access analytics showing registered site counts, preset/custom percentages, category distributions, and recently registered heritage landmarks.
2. **Comprehensive Sites Management (CRUD)**: Create, search, update, and delete landmarks.
3. **Multi-lingual Translations Hub**: Full localization panels for **French (FR)**, **English (EN)**, **German (DE)**, **Spanish (ES)**, and **Dutch (NL)**, allowing simultaneous editing of translated titles, descriptions, and narration texts.
4. **Interactive Tour Stops Builder**: Connect different registered landmarks into ordered paths (walking tour routes). Click landmarks in the catalog list to append them to the active chronological sequence easily.
5. **Universal Database Export**:
   - **SQL Insertion Scripts**: Export all edits as optimized raw SQL queries that can be ran instantly on any SQLite database instance.
   - **Consolidated JSON Data Packages**: Download a complete, unified multi-lingual JSON representation of sites and routes to consume via REST services.
6. **Self-Initializing SQLite Store**: The application uses a local SQLite file (`bourges_guide.db`) which automatically creates itself and seeds the default historical Bourges monuments and tour routes on first startup!

---

## Technical Architecture

- **Web framework**: ASP.NET Core 8.0 MVC
- **Database Context**: Entity Framework Core with Sqlite
- **Styling**: Bootstrap 5 + FontAwesome 6 icons + Google Plus Jakarta Sans font
- **JSON Serialization**: Newtonsoft.Json (handling array mappings and nested structures)

---

## Getting Started

### Prerequisites

You must have the **.NET 8.0 SDK** installed on your development machine. You can verify this by running:
```bash
dotnet --version
```

### Installation & Execution

1. Navigate to the project root directory:
   ```bash
   cd backend/BourgesAdminBackend
   ```

2. Restore NuGet dependencies:
   ```bash
   dotnet restore
   ```

3. Build and execute the application:
   ```bash
   dotnet run
   ```

4. Open your favorite browser and navigate to:
   ```
   http://localhost:5200  (or as specified in the CLI logs)
   ```

---

## Synchronizing with the Android App

Since the Android mobile guide application uses an offline **Room Database** (SQLite file in the assets folder or internal storage), you can easily update the app using our sync outputs:

### Method A: Static Asset Update (Recommended)

1. Open the Admin Panel and navigate to **Export & Deploy** in the sidebar.
2. Click **Download SQL Script** to export all your modifications as SQL commands.
3. Replace the corresponding tables in your mobile database or execute these commands on your master SQLite asset file.
4. Compile the APK again in AI Studio to ship the app with the freshly updated landmarks database as standard presets!

### Method B: JSON Feed Sync

1. Download the consolidated database as a JSON package by clicking **Export Consolidated JSON**.
2. Put the JSON file directly into the Android assets directory or consume it over the web using the `/api/export/json` endpoint inside the app's sync logic.
