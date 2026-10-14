import type { CategoryDTO } from '../model/CategoryDTO'

function isCategoryDTO(value: unknown): value is CategoryDTO {
  return (
    typeof value === 'object'
    && value !== null
    && 'id' in value
    && typeof value.id === 'number'
    && 'name' in value
    && typeof value.name === 'string'
    && 'type' in value
    && typeof value.type === 'string'
  )
}

export async function fetchCategoriesByType(
  type: string,
  signal?: AbortSignal,
): Promise<CategoryDTO[]> {
  const response = await fetch(
    `/api/1.0/categories/${encodeURIComponent(type)}`,
    { signal },
  )

  if (!response.ok) {
    throw new Error(`Categories request failed with HTTP ${response.status}`)
  }

  const data: unknown = await response.json()

  if (!Array.isArray(data) || !data.every(isCategoryDTO)) {
    throw new Error('Categories response has an invalid shape')
  }

  return data
}
