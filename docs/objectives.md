# Objectives

## Functional objectives

The main functional objective of Artseek is to provide a platform that facilitates the discovery of artworks and artists through intelligent searches. To achieve this, users will be able to use natural language, reference images, or both sources of information simultaneously. Artists will be able to manage their profiles and portfolios, while Artificial Intelligence will make it possible to analyze and categorize their works in order to improve their subsequent retrieval.

The main functional objectives are:

- Allow users to register, log in, and manage their profile.

- Allow artists to create and manage a portfolio of artworks.

- Allow images, titles, and descriptions to be associated with artworks.

- Analyze artworks using Artificial Intelligence to obtain characteristics and categories.

- Allow users to search for artworks using queries written in natural language.

- Allow users to search for artworks using reference images.

- Allow natural language and images to be combined in the same search.

- Display results ordered according to their degree of similarity to the query.

- Allow users to search for artists directly.

- Display the relationship between the artworks found and the artists who created them.



## Technical objectives

The technical objective is to build a maintainable and scalable SPA web application that clearly separates the user interface, business logic, persistence, and Artificial Intelligence services. The system will use a REST API developed with Java and Spring Boot and a frontend developed with React. The application will run using Docker containers and will include continuous integration through GitHub Actions.

The main technical objectives are:

- Develop the interface as an SPA using **React**, **Vite** and **React Router**. Using **Tailwind CSS** and libraries such as **React Flow**, **Lucide React**, **Motion for React**

- Implement the backend using **Java and Spring Boot** through a REST API.

- Use a relational **PostgresSQL** database to store users, artists, artworks, and categories.

- Incorporate a storage system for vector representations to perform semantic searches.

- Integrate Artificial Intelligence services or models to analyze images and generate semantic representations.

- Implement vector search based on similarity between embeddings.

- Design the search system to support text, images, and multimodal queries.

- Use **Docker** for containerizing the different components of the application.

- Use **GitHub Actions** to automate compilation, testing, and quality checks.

- Apply an automated testing strategy during development, using **JUnit** and **Spring Boot Test**.

- Ensure authentication and authorization, as well as role-based access control and resource ownership control with **Spring Security** and **JWT**.
