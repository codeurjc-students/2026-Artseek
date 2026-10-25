import { Alert, ListGroup } from 'react-bootstrap'
import { Link } from 'react-router'

import { usedCategoryTypesQueryOptions } from '../features/categories/queries/category-type-queries'
import { queryClient } from '../lib/query-client'
import type { Route } from './+types/HomeRoute'

export async function clientLoader() {
  const categoryTypes = await queryClient.query(usedCategoryTypesQueryOptions())
  return { categoryTypes }
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Artseek' },
    { name: 'description', content: 'Explore art by category with Artseek' },
  ]
}

export default function Home({ loaderData }: Route.ComponentProps) {
  const { categoryTypes } = loaderData

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

        {categoryTypes.length === 0 && (
          <Alert variant="secondary">No categories are currently available.</Alert>
        )}

        {categoryTypes.length > 0 && (
          <ListGroup>
            {categoryTypes.map((categoryType) => (
              <ListGroup.Item
                action
                as={Link}
                key={categoryType.value}
                to={`/category/${encodeURIComponent(categoryType.value)}`}
              >
                {categoryType.value}
              </ListGroup.Item>
            ))}
          </ListGroup>
        )}
      </section>
    </main>
  )
}
