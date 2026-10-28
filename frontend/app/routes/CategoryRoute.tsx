import { Alert, ListGroup } from 'react-bootstrap'
import { Link } from 'react-router'

import { fetchCategoriesByType } from '../features/categories/api/category-api'
import type { Route } from './+types/CategoryRoute'

export async function clientLoader({ params, request }: Route.ClientLoaderArgs) {
  const categories = await fetchCategoriesByType(params.categoryType, request.signal)
  return { categories }
}

export function meta({ params }: Route.MetaArgs) {
  return [
    { title: `${params.categoryType} categories · Artseek` },
    {
      name: 'description',
      content: `Explore ${params.categoryType} categories on Artseek`,
    },
  ]
}

export default function Category({ params, loaderData }: Route.ComponentProps) {
  const { categories } = loaderData

  return (
    <main>
      <header>
        <Link to="/">Back to category types</Link>
        <h1>{params.categoryType}</h1>
      </header>

      <section aria-labelledby="categories-heading">
        <h2 id="categories-heading">Categories</h2>

        {categories.length === 0 && (
          <Alert variant="secondary">
            No categories are currently available for this type.
          </Alert>
        )}

        {categories.length > 0 && (
          <ListGroup>
            {categories.map((category) => (
              <ListGroup.Item key={category.id}>{category.name}</ListGroup.Item>
            ))}
          </ListGroup>
        )}
      </section>
    </main>
  )
}
