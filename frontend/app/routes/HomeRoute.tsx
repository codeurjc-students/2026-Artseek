import Alert from 'react-bootstrap/Alert'
import ListGroup from 'react-bootstrap/ListGroup'
import Spinner from 'react-bootstrap/Spinner'
import { Link } from 'react-router'

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
          <Spinner animation="border" role="status" aria-label="Loading categories" />
        )}

        {categoryTypes.isError && (
          <Alert variant="danger">
            Categories are unavailable right now. Please try again later.
          </Alert>
        )}

        {categoryTypes.isSuccess && categoryTypes.data.length === 0 && (
          <Alert variant="secondary">No categories are currently available.</Alert>
        )}

        {categoryTypes.isSuccess && categoryTypes.data.length > 0 && (
          <ListGroup>
            {categoryTypes.data.map((categoryType) => (
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
