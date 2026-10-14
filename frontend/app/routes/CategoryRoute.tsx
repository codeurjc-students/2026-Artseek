import Alert from 'react-bootstrap/Alert'
import ListGroup from 'react-bootstrap/ListGroup'
import Spinner from 'react-bootstrap/Spinner'
import { Link } from 'react-router'

import { useCategoriesByTypeQuery } from '../features/categories/hooks/category-hooks'
import type { Route } from './+types/CategoryRoute'

export function meta({ params }: Route.MetaArgs) {
  return [
    { title: `${params.categoryType} categories · Artseek` },
    {
      name: 'description',
      content: `Explore ${params.categoryType} categories on Artseek`,
    },
  ]
}

export default function Category({ params }: Route.ComponentProps) {
  const categories = useCategoriesByTypeQuery(params.categoryType)

  return (
    <main>
      <header>
        <Link to="/">Back to category types</Link>
        <h1>{params.categoryType}</h1>
      </header>

      <section aria-labelledby="categories-heading">
        <h2 id="categories-heading">Categories</h2>

        {categories.isPending && (
          <Spinner animation="border" role="status" aria-label="Loading categories" />
        )}

        {categories.isError && (
          <Alert variant="danger">
            Categories are unavailable right now. Please try again later.
          </Alert>
        )}

        {categories.isSuccess && categories.data.length === 0 && (
          <Alert variant="secondary">
            No categories are currently available for this type.
          </Alert>
        )}

        {categories.isSuccess && categories.data.length > 0 && (
          <ListGroup>
            {categories.data.map((category) => (
              <ListGroup.Item key={category.id}>{category.name}</ListGroup.Item>
            ))}
          </ListGroup>
        )}
      </section>
    </main>
  )
}
