package org.molgenis.data.support;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.molgenis.data.AggregateAnonymizer;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AnonymizedAggregateResult;

import java.util.List;

public class AggregateAnonymizerImpl implements AggregateAnonymizer
{
	@Override
	public AnonymizedAggregateResult anonymize(final AggregateResult result, final int threshold)
	{
		List<List<Long>> anonymizedmatrix = Lists.newArrayList();

		for (List<Long> row : result.getMatrix())
		{
			List<Long> anonymizedRow = Lists.transform(row, new Function<Long, Long>()
			{
				@Override
				public Long apply(Long input)
				{
					if (input == null) return null;
					return input <= threshold ? AGGREGATE_ANONYMIZATION_VALUE : input;
				}

			});
			anonymizedmatrix.add(anonymizedRow);
		}

		return new AnonymizedAggregateResult(anonymizedmatrix, result.getxLabels(), result.getyLabels(), threshold);
	}
}
