# Copilot Instructions for Android Java Project

This project follows the MVVM architecture using Java, XML layouts, Retrofit and Moshi for networking, Dagger Hilt for dependency injection, and ViewBinding for UI components.

Wallflow project is a separate wallpaper kotlin jetpack compose project. You should use it as a reference for the UI design.

---

## Project Structure

- `data/remote/`

    - Contains Retrofit service interfaces for API endpoints.

- `data/repository/`

    - Houses repository classes that interact with Retrofit services.

- `di/`

    - Includes Dagger Hilt modules for providing dependencies like Retrofit instances.

- `model/`

    - Defines data models corresponding to API responses.

- `ui/`

    - `activity/`

        - Activities utilizing ViewBinding and observing ViewModels.

    - `fragment/`

        - Fragments with ViewBinding and ViewModel integration.

    - `adapter/`

        - Adapters for RecyclerViews or other UI components.

    - `viewmodel/`
        - ViewModels that interact with repositories and expose LiveData or StateFlow.

---

## Development Guidelines

### 1. Networking with Retrofit

- Define API endpoints in interfaces within `data/remote/`.
- Use Retrofit annotations to specify HTTP methods and endpoints.

### 2. Dependency Injection with Dagger Hilt

- Create modules in `di/` to provide Retrofit instances and other dependencies.
- Annotate modules with `@Module` and `@InstallIn(SingletonComponent.class)`.
- Provide dependencies using `@Provides` and annotate them with `@Singleton` if needed.

### 3. Repositories

- Implement repositories in `data/repository/` that utilize Retrofit services.
- Repositories should handle data operations and expose results to ViewModels.

### 4. ViewModels

- Place ViewModels in `ui/viewmodel/`.
- Inject repositories using Hilt's `@Inject` annotation.
- Expose data to the UI via LiveData.

### 5. UI Components

- Use ViewBinding in activities and fragments.

### 6. Getters and Setters

- Use lombok's `@Getter` and `@Setter` annotations for data models.

---

## Restrictions

- Do **not** use Retrofit services directly in activities, fragments, or ViewModels.
- All network operations must go through repositories.
