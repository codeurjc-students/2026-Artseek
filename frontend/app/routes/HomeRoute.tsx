import { useUsedCategoryTypesQuery } from '../features/categories/hooks/category-type-hooks'
import type { Route } from './+types/HomeRoute'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Artseek' },
    { name: 'description', content: 'Explore art by category with Artseek' },
  ]
}

export default function Home() {
  const categoryTypes = useUsedCategoryTypesQuery()

  return (
    <main>
      <header>
        <p>Explore · Connect · Create</p>
        <h1>Artseek</h1>
        <p>
          Discover the kinds of art currently represented in the collection.
        </p>
      </header>

      <section aria-labelledby="category-types-heading">
        <h2 id="category-types-heading">Categories</h2>

        {categoryTypes.isPending && (
          <p role="status">Loading categories…</p>
        )}

        {categoryTypes.isError && (
          <p role="alert">
            Categories are unavailable right now. Please try again later.
          </p>
        )}

        {categoryTypes.isSuccess && categoryTypes.data.length === 0 && (
          <p role="status">No categories are currently available.</p>
        )}

        {categoryTypes.isSuccess && categoryTypes.data.length > 0 && (
          <ul>
            {categoryTypes.data.map((categoryType) => (
              <li key={categoryType.value}>{categoryType.value}</li>
            ))}
          </ul>
        )}
      </section>
    </main>
  )
}
