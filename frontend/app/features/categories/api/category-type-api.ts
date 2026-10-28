import type { CategoryTypeDTO } from '../model/CategoryTypeDTO'
import { apiRequest } from '../../../utils/api-request'

function isCategoryTypeDTO(value: unknown): value is CategoryTypeDTO {
  return (
    typeof value === 'object'
    && value !== null
    && 'value' in value
    && typeof value.value === 'string'
  )
}

export async function fetchUsedCategoryTypes(
  signal?: AbortSignal,
): Promise<CategoryTypeDTO[]> {
  const data = await apiRequest<CategoryTypeDTO[]>('/api/v1/categories/types/used', { signal })

  if (!Array.isArray(data) || !data.every(isCategoryTypeDTO)) {
    throw new Error('Used category types response has an invalid shape')
  }

  return data
}
