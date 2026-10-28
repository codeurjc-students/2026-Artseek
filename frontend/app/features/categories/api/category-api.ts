import type { CategoryDTO } from '../model/CategoryDTO'
import { apiRequest } from '../../../utils/api-request'

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
  const data = await apiRequest<CategoryDTO[]>(
    `/api/v1/categories/${encodeURIComponent(type)}`,
    { signal },
  )

  if (!Array.isArray(data) || !data.every(isCategoryDTO)) {
    throw new Error('Categories response has an invalid shape')
  }

  return data
}
